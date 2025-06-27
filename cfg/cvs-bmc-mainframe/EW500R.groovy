import com.nib.groovybatch.NibJob
import com.nib.commons.io.tools.NibVsamCommandLine
import groovy.transform.BaseScript
import procs.DP999R
import procs.EW500R
import procs.PT525R


@BaseScript NibJob EW500R

// EW500R   JOB (PROD),'WIC APL/STR MAINT',MSGLEVEL=(1,1),                       
//              CLASS=P,MSGCLASS=J,COND=(0,NE),REGION=0M                         

job "EW500R", cond: [0,NE]

// unknown = "(PROD)"
// unknown = "'WIC APL/STR MAINT'"
// MSGLEVEL = "(1,1)"
// CLASS = "P"
// MSGCLASS = "J"
// REGION = "0M"

// *                                                                             
// *----------------------- CHANGE LOG --------------------------------*         
// * 09/19/14 - LCC -  NEW JOB PER CCB C34436                          *         
// * 05/29/24 - DMB -  COPIED PROCLIB - CHG0603968 - NEW FILES EWS791A/*         
// *                   EWS792A                                         *         
// *-------------- SCHEDULING INSTRUCTIONS FOR ABENDS -----------------*         
// *      CAN RUN ANY TIME OF THE DAY. MULTIPLE RUNS ALLOWED IN A DAY  *         
// *      IF THE JOB FAILS, CHECK THE CONTROL-CARD. IN CASE OF ANY     *         
// *      OTHER ISSUES, CONTACT APPLICATIONS AND NOTIFY USER           *         
// *      JOB IS NON-CRITICAL                                          *         
// *      NOTE: JOB CALLS PT525R REQUEST JOB TO ADD TO PTS105U FILE.   *         
// *-------------------------------------------------------------------*         
// * AUTO-EDIT VARIABLES - ASSIGN ODATE AS THE CVSDATE                           
// * %%SET %%A = %%SUBSTR %%ODATE 3 2                                            
// * %%SET %%B = %%SUBSTR %%ODATE 5 2                                            
// * %%SET %%C = %%SUBSTR %%ODATE 1 2                                            
// *--------------------------------------------------------------------*        
// *                                                                             
// //DP999R  EXEC DP999R,MEMBER='EW500R99'                                         

exec "DP999R", proc: procs.DP999R, MEMBER: "EW500R99", {
}

// *                                                                             
// //EW500R   EXEC EW500R,                                                         
// //             CVSDATE='%%A-%%B-%%C'                                            
// //*------------------------ CONTROL CARD ------------------------------*        
// //*  CC    1 - 5  = STORE NUMBER                                       *        
// //*        6 - 6  = FORCE STORE CONTROL FLAG ( Y OR N )                *        
// //*        7 - 7  = FORCE APL FLAG ( Y OR N )                          *        
// //* SAMPLE PARM -->  00008YN                                           *        
// //*--------------------------------------------------------------------*        
// //EW500N10.EWC500AI DD  *                                                       
// 06446YY                                                                         
// /*                                                                              

exec "EW500R", proc: procs.EW500R, CVSDATE: "%%A-%%B-%%C", {
    // Instream content is expanded below
    dd "EW500N10.EWC500AI", instream: [
    "06446YY                                                                 "
    ]
}

// *                                                                             
// //PT525R   EXEC PT525R,                                                         
// //         GDG00='00',                                                          
// //         GDG01='+01',                                                         
// //         JOBNAME=PT525R,                                                      
// //*-------------------------------------------------------------------*         
// //* PTS105SI                                                          *         
// //*        ADD THE TEST DATASET TO BE APPENDED TO PTS105U HERE.       *         
// //*        THE DATASET SHOULD BE OF 5BYTE STORE NUMBER LAYOUT.        * MXP04/09
// //*        PT105-STORE-NO-5BYTE FIELD SHOULD HOLD THE ACTUAL STORE    * MXP04/09
// //*        NUMBER.                                                    * MXP04/09
// //*---************************************************************----*         
// //*        *****DO NOT CHANGE DSN BELOW**** (STD)                     *         
// //*-------------------------------------------------------------------*         
// //         PTS105SI=PS.PTS905V.EW500.WIC.APL.TRANS                              

exec "PT525R", proc: procs.PT525R, GDG00: "00", GDG01: "+01", JOBNAME: "PT525R", PTS105SI: "PS.PTS905V.EW500.WIC.APL.TRANS", {
}

// *------------------------- STEP FUNCTION ---------------------------*         
// *  DELETE SCRATCH FILES                                             *         
// *-------------------------------------------------------------------*         
// //ALLOCN99 EXEC PGM=IEFBR14                                                     
// //DD01     DD  DSN=PS.PTS905V.EW500.WIC.APL.TRANS,                <ID>          
// //             DISP=(MOD,DELETE,DELETE)                                         

exec "ALLOCN99", pgm: "IEFBR14", {
    dd "DD01", dsn: "PS.PTS905V.EW500.WIC.APL.TRANS", disp: [MOD,DELETE,DELETE]
}

