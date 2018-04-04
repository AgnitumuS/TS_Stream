package com.excellence.iptv.util;

import android.util.Log;

import com.excellence.iptv.bean.Program;
import com.excellence.iptv.bean.tables.Pat;
import com.excellence.iptv.bean.tables.PatProgram;
import com.excellence.iptv.bean.tables.Sdt;
import com.excellence.iptv.bean.tables.SdtService;

import java.util.ArrayList;
import java.util.List;

import static java.lang.Integer.toHexString;

/**
 * ProgramManager
 *
 * @author ggz
 * @date 2018/4/4
 */

public class ProgramManager {
    private static final String TAG = "ProgramManager";

    private List<Program> mProgramList = new ArrayList<>();

    public ProgramManager() {
        super();
    }

    public List<Program> makeProgramList(Pat pat, Sdt sdt) {
        List<PatProgram> patProgramList = pat.getPatProgramList();
        List<SdtService> sdtServiceList = sdt.getSdtServiceList();
        for (int i = 0; i < patProgramList.size(); i++) {
            Log.d(TAG, " ---------------------------------------------- ");
            Log.d(TAG, " -- makeProgramList()");
            int programNumber = patProgramList.get(i).getProgramNumber();
            int programMapPid = patProgramList.get(i).getProgramMapPid();
            String programName = "UnKnow";
            for (int j = 0; j < sdtServiceList.size(); j++) {
                int serviceId = sdtServiceList.get(j).getServiceId();
                if (programNumber == serviceId) {
                    programName = sdtServiceList.get(j).getServiceName();
                }
            }
            Log.d(TAG, " -- ");
            Log.d(TAG, "programNumber : 0x" + toHexString(programNumber));
            Log.d(TAG, "programMapPid : 0x" + toHexString(programMapPid));
            Log.d(TAG, "programName : " + programName);
            Program program = new Program(programNumber, programMapPid, programName);
            mProgramList.add(program);
        }

        return mProgramList;
    }
}
