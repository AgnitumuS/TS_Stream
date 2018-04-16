package com.excellence.iptv.util;

import android.util.Log;

import com.excellence.iptv.bean.Program;
import com.excellence.iptv.bean.tables.Eit;
import com.excellence.iptv.bean.tables.EitEvent;
import com.excellence.iptv.bean.tables.Pat;
import com.excellence.iptv.bean.tables.PatProgram;
import com.excellence.iptv.bean.tables.Sdt;
import com.excellence.iptv.bean.tables.SdtService;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
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

    public List<Program> makeProgramList(Pat pat, Sdt sdt, Eit eit) {
        List<PatProgram> patProgramList = pat.getPatProgramList();
        List<SdtService> sdtServiceList = sdt.getSdtServiceList();
        List<EitEvent> eitEventList = eit.getEitEventList();

        for (int i = 0; i < patProgramList.size(); i++) {
            Log.d(TAG, " ---------------------------------------------- ");
            Log.d(TAG, " -- makeProgramList()");
            int programNumber = patProgramList.get(i).getProgramNumber();
            int programMapPid = patProgramList.get(i).getProgramMapPid();
            String programName = "UnKnow";
            String startTime = "null";
            String duration = "null";
            String endTime = "null";
            String eventName = "UnKnow";
            for (int j = 0; j < sdtServiceList.size(); j++) {
                int serviceId = sdtServiceList.get(j).getServiceId();
                if (programNumber == serviceId) {
                    programName = sdtServiceList.get(j).getServiceName();
                    break;
                }
            }
            for (int j = 0; j < eitEventList.size(); j++) {
                int serviceId = eitEventList.get(j).getServiceId();
                int runningStatus = eitEventList.get(j).getRunningStatus();
                if (programNumber == serviceId && runningStatus == 4) {

                    byte[] startTimeBcd = eitEventList.get(j).getStartTimeBcd();
                    startTime = bcd2Str(startTimeBcd);

                    byte[] durationBcd = eitEventList.get(j).getDurationBcd();
                    duration = bcd2Str(durationBcd);

                    try {
                        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
                        long result = sdf.parse(startTime).getTime() + sdf.parse(duration).getTime();
                        Date date = new Date(result);
                        endTime = sdf.format(date).toString();
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }

                    eventName = eitEventList.get(j).getEventName();
                    break;
                }
            }

            Log.d(TAG, " -- ");
            Log.d(TAG, "programNumber : 0x" + toHexString(programNumber));
            Log.d(TAG, "programMapPid : 0x" + toHexString(programMapPid));
            Log.d(TAG, "programName : " + programName);
            Log.d(TAG, "startTime : " + startTime);
            Log.d(TAG, "duration : " + duration);
            Log.d(TAG, "endTime : " + endTime);
            Log.d(TAG, "eventName : " + eventName);
            Program program = new Program(programNumber, programMapPid, programName,
                    startTime, duration, endTime, eventName);
            mProgramList.add(program);
        }

        return mProgramList;
    }

    private String bcd2Str(byte[] bytes) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < bytes.length; i++) {
            builder.append(toHexString((bytes[i] >> 4) & 0xF));
            builder.append(toHexString(bytes[i] & 0xF));
            if (i < bytes.length - 1) {
                builder.append(":");
            }
        }
        return builder.toString();
    }

}
