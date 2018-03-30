package com.tosmart.tsgetpidpacket.beans;

import com.bigkoo.pickerview.model.IPickerViewData;

import java.util.List;

/**
 * MainActivity
 *
 * @author ggz
 * @date 2018/3/24
 */

public class Picker implements IPickerViewData {

    private String pid;
    private List<String> tableId;

    @Override
    public String getPickerViewText() {
        return this.pid;
    }

    public String getPid() {
        return pid;
    }

    public void setPid(String pid) {
        this.pid = pid;
    }

    public List<String> getTableId() {
        return tableId;
    }

    public void setTableId(List<String> tableId) {
        this.tableId = tableId;
    }
}
