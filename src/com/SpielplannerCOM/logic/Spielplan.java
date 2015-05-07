/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.SpielplannerCOM.logic;

import com.SpielplannerCOM.UI.SpeilplanUI;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.Random;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import static java.nio.charset.StandardCharsets.ISO_8859_1;
import static java.nio.charset.StandardCharsets.UTF_8;

/**
 *
 * @author mazy
 */
public class Spielplan {

    /**
     * @param args the command line arguments
     */
    private static File inFile;
    private static File outFile;
    private static String leagueName;
    private static String season;
    private static String startDateStr;
    private static String endDateStr;
    private static Date startDateDt;
    private static Date endDateDt;
    public static ArrayList<String> pairTeams = new ArrayList<String>();
    public static ArrayList<Date> sundaysDt = new ArrayList<Date>();
    public static ArrayList<String> sundaysStr = new ArrayList<String>();
    public static ArrayList<String> teamNames = new ArrayList<String>();

    public static void spielplannerMain() {
        clearArrays();
//        
//                        /* Create and display the form */
//                        java.awt.EventQueue.invokeLater(new Runnable() {
//                        public void run() {
//                        new SpeilplanUI().setVisible(true);
//                            }
//                        });
        leagueName = null;
        season = null;
        startDateStr = null;
        endDateStr = null;

        //Read From JSON File
        JSONParser parser = new JSONParser();
        try {
            Object myObj = parser.parse(new InputStreamReader(new FileInputStream(getInFile()), ISO_8859_1));
            JSONObject myJSONobj = (JSONObject) myObj;

            leagueName = (String) myJSONobj.get("league");
            season = (String) myJSONobj.get("season");
            startDateStr = (String) myJSONobj.get("start");
            endDateStr = (String) myJSONobj.get("end");
            byte[] ptex = leagueName.getBytes("UTF-8");

            JSONArray teamsArr = (JSONArray) myJSONobj.get("teams");
            for (Object cTeam : teamsArr) {
                JSONObject teamJSONobj = (JSONObject) cTeam;
                String teamName = (String) teamJSONobj.get("name");
                teamNames.add(teamName);

            }
        } catch (IOException | org.json.simple.parser.ParseException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        //team Pairs creator
        pairTeamMaker(teamNames);
        //Sunday Finder
        sundaysFinder();

        //Convert All Sundays from Date format to String format
        for (Date d : sundaysDt) {
            String t = dateToString(d);
            sundaysStr.add(t);
        }

        int pairTeamsSize = pairTeams.size() - 1;
        //Pick a random Sunday  and also a random TeamPair 
        try (Writer writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(getOutFile()), "utf-8"))) {
            writer.write(" \n \n -----**** " + leagueName + "  League plan " + season + " ****-----\n"
                    + " \n"
                    + " The league starts at: " + startDateStr + " and end at: " + endDateStr + " \n \n"
                    + " We have the following teams : \n");
            for (String team : teamNames) {
                writer.write(" " + team + "\n");
            }
            writer.write("\n and the games plan is as the following in the following days: \n \n");
            for (int i = 0; i < pairTeamsSize; i++) {
                int randomPairTeamIndex = randInt(0, pairTeams.size() - 1);
                String pairTeam = pairTeams.get(randomPairTeamIndex);
                pairTeams.remove(randomPairTeamIndex);
                int randomSundayIndex = randInt(0, sundaysStr.size() - 1);
                String sunday = sundaysStr.get(randomSundayIndex);
                sundaysStr.remove(randomSundayIndex);
//				System.out.println(sunday + " | " + pairTeam);
                writer.write(sunday + " | " + pairTeam + "\n");

            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private static void sundaysFinder() {
        startDateDt = stringToDate(startDateStr);
        endDateDt = stringToDate(endDateStr);

        Calendar calStart = Calendar.getInstance();
        calStart.setTime(startDateDt);
        Calendar calEnd = Calendar.getInstance();
        calEnd.setTime(endDateDt);

        while (calEnd.after(calStart)) {

            if (calStart.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY) {
                Date sundaysDate = calStart.getTime();
                sundaysDt.add(sundaysDate);
            }
            calStart.add(Calendar.DATE, 1);
        }

    }

    private static void pairTeamMaker(ArrayList<String> teamNames) {
        for (int i = 0; i < teamNames.size(); i++) {
            for (int j = 0; j < teamNames.size(); j++) {
                if (i == j) { // do nothing
                } else {
                    pairTeams.add(teamNames.get(i) + " - " + teamNames.get(j));
                }
            }
        }

    }

    public static int randInt(int min, int max) {

	    // NOTE: Usually this should be a field rather than a method
        // variable so that it is not re-seeded every call.
        Random rand = new Random();

	    // nextInt is normally exclusive of the top value,
        // so add 1 to make it inclusive
        int randomNum = rand.nextInt((max - min) + 1) + min;

        return randomNum;
    }

    public static String dateToString(Date inputDateAsDt) {
        String dateStr = null;
        DateFormat frmt = new SimpleDateFormat("EEE dd.MM.yyyy", Locale.GERMAN);
        dateStr = frmt.format(inputDateAsDt);
        return dateStr;
    }

    public static Date stringToDate(String inputDateAsStr) {
        DateFormat frmt = new SimpleDateFormat("yyyy-MM-dd", Locale.GERMAN);
        Date date = null;
        String frmtDate = null;
        try {
            date = frmt.parse(inputDateAsStr);
            frmtDate = frmt.format(date);
        } catch (java.text.ParseException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return date;
    }

    /**
     * @return the inFile
     */
    public static File getInFile() {
        return inFile;
    }

    /**
     * @param aInFile the inFile to set
     */
    public static void setInFile(File aInFile) {
        inFile = aInFile;
    }

    /**
     * @return the outFile
     */
    public static File getOutFile() {
        return outFile;
    }

    /**
     * @param aOutFile the outFile to set
     */
    public static void setOutFile(File aOutFile) {
        outFile = aOutFile;
    }

    private static void clearArrays() {
        sundaysDt.clear();
        sundaysStr.clear();
        pairTeams.clear();
        teamNames.clear();
    }

}
