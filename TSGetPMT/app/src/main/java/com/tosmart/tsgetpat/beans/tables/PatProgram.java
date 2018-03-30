package com.tosmart.tsgetpat.beans.tables;

/**
 * PatProgram
 *
 * @author ggz
 * @date 2018/3/27
 */

public class PatProgram {

    /**
     * program_number : 16 bit
     */
    private int programNumber;

    /**
     * program_map_PID : 13 bit
     */
    private int programMapPid;


    public PatProgram(int programNumber, int programMapPid) {
        super();
        this.programNumber = programNumber;
        this.programMapPid = programMapPid;
    }

    public int getProgramNumber() {
        return programNumber;
    }

    public void setProgramNumber(int programNumber) {
        this.programNumber = programNumber;
    }

    public int getProgramMapPid() {
        return programMapPid;
    }

    public void setProgramMapPid(int programMapPid) {
        this.programMapPid = programMapPid;
    }
}
