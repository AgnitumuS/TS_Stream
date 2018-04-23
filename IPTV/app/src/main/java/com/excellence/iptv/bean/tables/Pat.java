package com.excellence.iptv.bean.tables;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Pat
 *
 * @author ggz
 * @date 2018/3/27
 */

public class Pat implements Serializable {

    private int tableId = 0x00;

    private int networkPid;

    private List<PatProgram> patProgramList = new ArrayList<>();

    public Pat() {
        super();
    }

    public int getTableId() {
        return tableId;
    }

    public int getNetworkPid() {
        return networkPid;
    }

    public void setNetworkPid(int networkPid) {
        this.networkPid = networkPid;
    }

    public List<PatProgram> getPatProgramList() {
        return patProgramList;
    }

    public void setPatProgramList(List<PatProgram> patProgramList) {
        this.patProgramList = patProgramList;
    }

}
