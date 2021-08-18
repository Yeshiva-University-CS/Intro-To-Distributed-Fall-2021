package edu.yu.cs.com3800;

/**
 * a vote for a given machine ID to be the leader in a given epoch.
 */
public class Vote {
    final private long proposedLeaderID;
    final private long peerEpoch;

    public Vote(long proposedLeaderID, long peerEpoch) {
        this.proposedLeaderID = proposedLeaderID;
        this.peerEpoch = peerEpoch;
    }

    public long getProposedLeaderID() {
        return proposedLeaderID;
    }

    public long getPeerEpoch() {
        return peerEpoch;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Vote)) {
            return false;
        }
        Vote other = (Vote) o;
        return (proposedLeaderID == other.proposedLeaderID && peerEpoch == other.peerEpoch);
    }

    @Override
    public int hashCode() {
        return (int) (proposedLeaderID);
    }

    public String toString() {
        return "(" + proposedLeaderID + ", " + Long.toHexString(peerEpoch) + ")";
    }
}
