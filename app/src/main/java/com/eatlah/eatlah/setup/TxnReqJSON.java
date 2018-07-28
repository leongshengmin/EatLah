package com.eatlah.eatlah.setup;

public class TxnReqJSON {
    String ss = "1";
    MSG msg;

    public class MSG {
        String txnAmount;   // transaction amt in cents
        String merchantTxnRef;  // unique order identifier (order timestamp)
        String b2sTxnEndURL;
        String s2sTxnEndURL;
        String netsMid; // nets merchant id
        String merchantTxnDtm;  // time of transaction yyyyMMdd HH:mm:ss.SSS

        final String mobileOs = "ANDROID";
        final String submissionMode = "B";
        final String paymentType = "SALE";
        final String paymentMode = "";
        final String clientType = "S";

        final String currencyCode = "SGD";
        final String merchantTimeZone = "+8:00";
        final String language = "en";
        final String netsMidIndicator = "U";


    }

    public TxnReqJSON() {
        this.msg = new MSG();
    }

    public void setMSG(String txnAmount, String merchantTxnRef, String netsMid, String merchantTxnDtm) {
        msg.txnAmount = txnAmount;
        msg.merchantTxnRef = merchantTxnRef;
        msg.netsMid = netsMid;
        msg.merchantTxnDtm = merchantTxnDtm;
    }

//    "msg":{
//        "txnAmount":"1000",<replace>
//                "merchantTxnRef":"20170605 10:36:51.98",<replace>
//                "b2sTxnEndURL":"https://sit2.enets.sg/MerchantApp/sim/b2sTxnEndURL.jsp",<replace>
//                "s2sTxnEndURL":"https://sit2.enets.sg/MerchantApp/rest/s2sTxnEnd",<replace>
//                "netsMid":"UMID_887770001",<replace>
//                "merchantTxnDtm":"20170605 10:36:51.989",<replace>
//
//                "mobileOs":"ANDROID"<default value1>
//                "submissionMode":"B",<default value1>
//                "paymentType":"SALE",<default value1>
//                "paymentMode":"",<default value1>
//                "clientType":"S",<default value1>
//
//                "currencyCode":"SGD",<default value2>
//                "merchantTimeZone":"+8:00",<default value2>
//                "language":"en", <default value2>
//                "netsMidIndicator":"U",<default value2>
}
