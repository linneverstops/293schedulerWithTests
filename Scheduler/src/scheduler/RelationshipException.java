package scheduler;

/**
 * TungHo Lin
 * txl429
 * PA7
 * Class RelationshipException
 */

//no test case needed for this class
public class RelationshipException extends Exception {

    public enum ErrorCode {
        CIRCULAR_REQUIREMENT, CONTRADICTING_REQUIREMENT;
    }

    private final ErrorCode errorCode;

    RelationshipException(ErrorCode errorCode) {
        this.errorCode = errorCode;
    }

    //autogenerated: no testing required
    @Override
    public String toString() {
        return "RelationshipException{" +
                "errorCode=" + errorCode +
                '}';
    }
}
