package com.excellence.iptv.bean.tables;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Sdt
 *
 * @author ggz
 * @date 2018/3/31
 */

public class Sdt implements Serializable {

    private int tableId = 0x42;

    List<SdtService> sdtServiceList = new ArrayList<>();

    public Sdt() {
        super();
    }

    public int getTableId() {
        return tableId;
    }

    public List<SdtService> getSdtServiceList() {
        return sdtServiceList;
    }

    public void setSdtServiceList(List<SdtService> sdtServiceList) {
        this.sdtServiceList = sdtServiceList;
    }
}
