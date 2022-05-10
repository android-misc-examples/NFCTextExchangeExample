package it.pgp.nfctextexchangeexample;

import android.app.Activity;
import android.content.Intent;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.NfcAdapter.CreateNdefMessageCallback;
import android.nfc.NfcAdapter.OnNdefPushCompleteCallback;
import android.nfc.NfcEvent;
import android.os.Bundle;
import android.os.Parcelable;
import android.widget.TextView;
import android.widget.Toast;

// TODO ask to switch on NFC if not active

public class ExchangeActivity extends Activity implements
        CreateNdefMessageCallback, OnNdefPushCompleteCallback{

    TextView status;

    NfcAdapter nfcAdapter;

    public static boolean sent = false;
    public static boolean received = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_exchange);
        status = findViewById(R.id.statusTextView);

        nfcAdapter = NfcAdapter.getDefaultAdapter(this);
        if(nfcAdapter==null){
            Toast.makeText(ExchangeActivity.this,
                    "no NFC adapter found on device, exiting...",
                    Toast.LENGTH_LONG).show();
            finish();
        }
//        else if (!nfcAdapter.isEnabled()) {
//            startActivityForResult(new Intent(android.provider.Settings.ACTION_NFC_SETTINGS),0x101);
//        }
        else{
//            Toast.makeText(ExchangeActivity.this,
//                    "Set Callback(s)",
//                    Toast.LENGTH_LONG).show();
            nfcAdapter.setNdefPushMessageCallback(this, this);
            nfcAdapter.setOnNdefPushCompleteCallback(this, this);
        }
    }

//    @Override
//    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
//        if (!nfcAdapter.isEnabled()) {
//            Toast.makeText(this, "NFC must be on, exiting...", Toast.LENGTH_SHORT).show();
//            finish();
//        }
//        else {
//            nfcAdapter.setNdefPushMessageCallback(this, this);
//            nfcAdapter.setOnNdefPushCompleteCallback(this, this);
//        }
//    }

    @Override
    protected void onResume() {
        super.onResume();

        Intent intent = getIntent();
        String action = intent.getAction();
        // upon tag receive
        if(action != null)
        if(action.equals(NfcAdapter.ACTION_NDEF_DISCOVERED) && !received){
            // receiving data
            Parcelable[] parcelables =
                    intent.getParcelableArrayExtra(
                            NfcAdapter.EXTRA_NDEF_MESSAGES);
            NdefMessage inNDefMessage = (NdefMessage)parcelables[0];
            NdefRecord[] inNDefRecords = inNDefMessage.getRecords();
            NdefRecord NDefRecord_0 = inNDefRecords[0];
            // bReceived = NDefRecord_0.getPayload();
            StaticHelper.bReceived = NDefRecord_0.getPayload();
            received = true;
            // wait for reply if this device were not the initiator of the data exchange
            if (sent) {
                // status.setText("Completed");
//                Intent i = getIntent();
//                i.putExtra(receivedDataTag,bReceived);
//                i.putExtra(senderDataTag,bToSend);
//                setResult(RESULT_OK,i);
                // finish(); // back to calling activity
                Intent intent1 = new Intent(ExchangeActivity.this,MainActivity.class);
                intent1.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK);
                intent1.putExtra("SENT",StaticHelper.bToSend);
                intent1.putExtra("REC",StaticHelper.bReceived);
                startActivity(intent1);
                return;
            }
            status.setText("Received data, now use this device as sender");
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        setIntent(intent);
    }

    @Override
    public void onNdefPushComplete(NfcEvent event) {

//        final String eventString = "onNDefPushComplete\n" + event.toString();
        runOnUiThread(new Runnable() {

            @Override
            public void run() {
//                Toast.makeText(getApplicationContext(),
//                        eventString,
//                        Toast.LENGTH_LONG).show();
                sent = true;

                if(received) {
//                    Intent i = getIntent();
//                    i.putExtra(receivedDataTag,bReceived);
//                    i.putExtra(senderDataTag,bToSend);
//                    setResult(RESULT_OK,i);
                    // finish(); // back to calling activity
                    Intent intent1 = new Intent(ExchangeActivity.this,MainActivity.class);
                    intent1.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    intent1.putExtra("SENT",StaticHelper.bToSend);
                    intent1.putExtra("REC",StaticHelper.bReceived);
                    startActivity(intent1);
                    return;
                }

                status.setText("Data sent, use this device as receiver now");
            }
        });

    }

    @Override
    public NdefMessage createNdefMessage(NfcEvent event) {
        byte[] bytesOut = StaticHelper.bToSend;

        NdefRecord ndefRecordOut = new NdefRecord(
                NdefRecord.TNF_MIME_MEDIA,
                "application/octet-stream".getBytes(),
                new byte[] {},
                bytesOut);

        return new NdefMessage(ndefRecordOut);
    }

}