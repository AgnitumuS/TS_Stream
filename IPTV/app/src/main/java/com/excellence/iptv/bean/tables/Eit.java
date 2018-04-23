package com.excellence.iptv.bean.tables;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Eit
 *
 * @author ggz
 * @date 2018/4/12
 */

public class Eit implements Serializable {

    private List<EitEvent> eitEventList = new ArrayList<>();

    public Eit() {
        super();
    }
    
    public List<EitEvent> getEitEventList() {
        return eitEventList;
    }

    public void setEitEventList(List<EitEvent> eitEventList) {
        this.eitEventList = eitEventList;
    }
}
