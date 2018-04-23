package com.excellence.iptv.util;

import android.util.Log;
import android.util.SparseArray;
import android.util.SparseIntArray;

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
    private static final boolean IS_LOG = false;

    private List<Program> mProgramList = new ArrayList<>();

    public ProgramManager() {
        super();
    }

    public List<Program> makeProgramList(Pat pat, Sdt sdt, Eit eit) {
        if (IS_LOG) {
            Log.d(TAG, " ---------------------------------------------- ");
            Log.d(TAG, " -- makeProgramList()");
        }
        List<PatProgram> patProgramList = pat.getPatProgramList();
        List<SdtService> sdtServiceList = sdt.getSdtServiceList();
        List<EitEvent> eitEventList = eit.getEitEventList();

        // 遍历 sdtServiceList ，记录每个 serviceId 的 position
        SparseIntArray sdtServiceIdPosition = new SparseIntArray();
        for (int i = 0; i < sdtServiceList.size(); i++) {
            int serviceId = sdtServiceList.get(i).getServiceId();
            sdtServiceIdPosition.put(serviceId, i);
        }

        // 遍历 eitEventList ，记录每个 serviceId 的 position
        SparseIntArray eitServiceIdPosition = new SparseIntArray();
        for (int i = 0; i < eitEventList.size(); i++) {
            int serviceId = eitEventList.get(i).getServiceId();
            int runningStatus = eitEventList.get(i).getRunningStatus();
            if (runningStatus == 4) {
                eitServiceIdPosition.put(serviceId, i);
            }
        }

        // 开始合成 ProgramList
        for (int i = 0; i < patProgramList.size(); i++) {
            int programNumber = patProgramList.get(i).getProgramNumber();
            int programMapPid = patProgramList.get(i).getProgramMapPid();
            String programName = "UnKnow";
            String startTime = "null";
            String duration = "null";
            String endTime = "null";
            String eventName = "UnKnow";

            int position = sdtServiceIdPosition.get(programNumber, -1);
            if (position != -1) {
                programName = sdtServiceList.get(position).getServiceName();
            }

            position = eitServiceIdPosition.get(programNumber, -1);
            if (position != -1) {
                byte[] startTimeBcd = eitEventList.get(position).getStartTimeBcd();
                startTime = bcd2Str(startTimeBcd);

                byte[] durationBcd = eitEventList.get(position).getDurationBcd();
                duration = bcd2Str(durationBcd);

                try {
                    SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
                    long result = sdf.parse(startTime).getTime() + sdf.parse(duration).getTime();
                    Date date = new Date(result);
                    endTime = sdf.format(date).toString();
                } catch (ParseException e) {
                    e.printStackTrace();
                }

                eventName = eitEventList.get(position).getEventName();
            }

            if (IS_LOG) {
                Log.d(TAG, " -- ");
                Log.d(TAG, "programNumber : 0x" + toHexString(programNumber));
                Log.d(TAG, "programMapPid : 0x" + toHexString(programMapPid));
                Log.d(TAG, "programName : " + programName);
                Log.d(TAG, "startTime : " + startTime);
                Log.d(TAG, "duration : " + duration);
                Log.d(TAG, "endTime : " + endTime);
                Log.d(TAG, "eventName : " + eventName);
            }

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
