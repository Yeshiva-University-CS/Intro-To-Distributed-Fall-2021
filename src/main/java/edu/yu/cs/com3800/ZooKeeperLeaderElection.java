package edu.yu.cs.com3800;

import java.util.concurrent.LinkedBlockingQueue;

public class ZooKeeperLeaderElection
{
    /**
     * time to wait once we believe we've reached the end of leader election.
     */
    private final static int finalizeWait = 200;

    /**
     * Upper bound on the amount of time between two consecutive notification checks.
     * This impacts the amount of time to get the system up again after long partitions. Currently 60 seconds.
     */
    private final static int maxNotificationInterval = 60000;

    public ZooKeeperLeaderElection(ZooKeeperPeerServer server, LinkedBlockingQueue<Message> incomingMessages)
    {
        this.incomingMessages = incomingMessages;
        this.myPeerServer = server;
    }

    private synchronized Vote getCurrentVote() {
        return new Vote(this.proposedLeader, this.proposedEpoch);
    }

    public synchronized Vote lookForLeader()
    {
        //send initial notifications to other peers to get things started
        sendNotifications();
        //Loop, exchanging notifications with other servers until we find a leader
        while (this.myPeerServer.getPeerState() == ZooKeeperPeerServer.ServerState.LOOKING)
            //Remove next notification from queue, timing out after 2 times the termination time
            //if no notifications received..
                //..resend notifications to prompt a reply from others..
                //.and implement exponential back-off when notifications not received..
            //if/when we get a message and it's from a valid server and for a valid server..
            //switch on the state of the sender:
                case LOOKING: //if the sender is also looking
                    //if the received message has a vote for a leader which supersedes mine, change my vote and tell all my peers what my new vote is.
                    //keep track of the votes I received and who I received them from.
                    ////if I have enough votes to declare my currently proposed leader as the leader:
                        //first check if there are any new votes for a higher ranked possible leader before I declare a leader. If so, continue in my election loop
                        //If not, set my own state to either LEADING (if I won the election) or FOLLOWING (if someone lese won the election) and exit the election
                case FOLLOWING: case LEADING: //if the sender is following a leader already or thinks it is the leader
                    //IF: see if the sender's vote allows me to reach a conclusion based on the election epoch that I'm in, i.e. it gives the majority to the vote of the FOLLOWING or LEADING peer whose vote I just received.
                        //if so, accept the election winner.
                        //As, once someone declares a winner, we are done. We are not worried about / accounting for misbehaving peers.
                     //ELSE: if n is from a LATER election epoch
                        //IF a quorum from that epoch are voting for the same peer as the vote of the FOLLOWING or LEADING peer whose vote I just received.
                           //THEN accept their leader, and update my epoch to be their epoch
                        //ELSE:
                            //keep looping on the election loop.    
    }

    private Vote acceptElectionWinner(ElectionNotification n)
    {
        //set my state to either LEADING or FOLLOWING
        //clear out the incoming queue before returning
    }

    /*
     * We return true if one of the following three cases hold:
     * 1- New epoch is higher
     * 2- New epoch is the same as current epoch, but server id is higher.
     */
     protected boolean supersedesCurrentVote(long newId, long newEpoch) {
         return (newEpoch > this.proposedEpoch) || ((newEpoch == this.proposedEpoch) && (newId > this.proposedLeader));
     }
    /**
     * Termination predicate. Given a set of votes, determines if have sufficient support for the proposal to declare the end of the election round.
     * Who voted for who isn't relevant, we only care that each server has one current vote
     */
    protected boolean haveEnoughVotes(Map<Long, ElectionNotification > votes, Vote proposal)
    {
       //is the number of votes for the proposal > the size of my peer server’s quorum?
    }
