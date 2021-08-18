package edu.yu.cs.com3800.stage2.udp_example;

import edu.yu.cs.com3800.Message;
import edu.yu.cs.com3800.UDPMessageReceiver;
import edu.yu.cs.com3800.UDPMessageSender;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;

public class ExamplePeerServer implements Runnable
{
    private LinkedBlockingQueue<Message> outgoingMessages;
    private LinkedBlockingQueue<Message> incomingMessages;
    private final InetSocketAddress myAddress;
    private List<InetSocketAddress> peers;
    private int myPort;

    public ExamplePeerServer(int myPort, List<InetSocketAddress> peers)
    {
        this.outgoingMessages = new LinkedBlockingQueue<>();
        this.incomingMessages = new LinkedBlockingQueue<>();
        this.myAddress = new InetSocketAddress("localhost",myPort);
        this.myPort = myPort;
        this.peers = peers;
        this.peers.remove(this.myAddress);
    }
    
    @Override
    public void run()
    {
        try{
            //step 1: create and run thread that sends broadcast messages
            UDPMessageSender sender = new UDPMessageSender(this.outgoingMessages,this.myPort);
            sender.start();
            //step 2: create and run thread that listens for messages sent to this server
            UDPMessageReceiver receiver = new UDPMessageReceiver(this.incomingMessages,this.myAddress,this.myPort,null);
            receiver.start();
        }catch(IOException e){
            e.printStackTrace();
            return;
        }
        //step 3: process received messages
        while(true)
        {
            try
            {
                Message msg = this.incomingMessages.take();
                System.out.println("@" + myAddress.getPort() + ": RECEIVED message from client at " + msg.getSenderHost() +
                        ":" + msg.getSenderPort() + ". Message: " + new String(msg.getMessageContents()));
            }
            catch (Exception e)
            {
                e.printStackTrace();
                return;
            }
        }
    }
    /**
     *
     * @param contents
     */
    public void sendBroadcast(byte[] contents)
    {
        for(InetSocketAddress peer : peers)
        {
            Message msg = new Message(Message.MessageType.WORK, contents,this.myAddress.getHostString(),this.myPort,peer.getHostString(),peer.getPort());
            this.outgoingMessages.offer(msg);
        }
    }
}