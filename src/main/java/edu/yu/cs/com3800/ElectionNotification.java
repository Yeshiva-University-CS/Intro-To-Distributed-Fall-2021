package edu.yu.cs.com3800;

/**
 * Used to communicate votes across servers.
 * Adds information to the Vote about the server casting the vote.
 */
public class ElectionNotification extends Vote {
    //ID of the sender
    final private long senderID;
    //state of the sender
    final private ZooKeeperPeerServer.ServerState state;

    public ElectionNotification(long proposedLeaderID, ZooKeeperPeerServer.ServerState state, long senderID, long peerEpoch) {
        super(proposedLeaderID, peerEpoch);
        this.senderID = senderID;
        this.state = state;
    }

    public long getSenderID() {
        return senderID;
    }

    public ZooKeeperPeerServer.ServerState getState() {
        return state;
    }

    public boolean equals(Object other) {
        if (!super.equals(other)) {
            return false;
        }
        if (!(other instanceof ElectionNotification)) {
            return false;
        }
        ElectionNotification otherEN = (ElectionNotification) other;
        if (this.senderID == otherEN.senderID && this.state == otherEN.state) {
            return true;
        }
        return false;
    }
}