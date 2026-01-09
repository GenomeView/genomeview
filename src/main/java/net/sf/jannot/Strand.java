/**
 * %HEADER%
 */
package net.sf.jannot;

public enum Strand {

    FORWARD, REVERSE, UNKNOWN;

    public int getValue() {
        switch (this) {
        case FORWARD:
            return 1;
        case REVERSE:
            return -1;
        case UNKNOWN:
            return 0;
        default:
            throw new RuntimeException("This is impossible!!!");
        }

    }

    static public Strand get(int i) {
        switch (i) {
        case 0:
            return UNKNOWN;
        case 1:
            return FORWARD;
        case -1:
            return REVERSE;
        default:
            return UNKNOWN;
        }
    }

    public static Strand fromSymbol(char c){
    	switch(c){
    	case '+':
    		return FORWARD;
    	case '-':
    		return REVERSE;
    	default:
    		return UNKNOWN;
    	}
    }
    
    /**
     * Symbol representing the Strand. Can be either + (plus), - (minus) or . (dot)
     *  
     * @return symbol representation of the strand
     */
    public String symbol() {
        switch (this) {
        case FORWARD:
            return "+";
        case REVERSE:
            return "-";
        case UNKNOWN:
            return ".";
        default:
            throw new RuntimeException("This is impossible!!!");
        }
    }
}
