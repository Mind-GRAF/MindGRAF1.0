package edu.guc.mind_graf.compression;

import java.util.Objects;

public class HypNode implements Comparable {
    int nodeID;
    int attitudeID;

    public HypNode(int nodeID, int attitudeID) {
        this.nodeID = nodeID;
        this.attitudeID = attitudeID;
    }

    @Override
    public int compareTo(Object arg0) {
        HypNode other = (HypNode) arg0;
        if (other.nodeID == nodeID && other.attitudeID == attitudeID) {
            return 0;
        }
        return -1;
    }

    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null || getClass() != obj.getClass())
            return false;
        HypNode myClass = (HypNode) obj;
        return Objects.equals(nodeID, myClass.nodeID) &&
                Objects.equals(attitudeID, myClass.attitudeID);
    }

    public int hashCode() {
        return Objects.hash(nodeID, attitudeID);
    }

    @Override
    public String toString() {
        return "HypNode{" +
                "nodeID=" + nodeID +
                ", attitudeID=" + attitudeID +
                '}';
    }

}
