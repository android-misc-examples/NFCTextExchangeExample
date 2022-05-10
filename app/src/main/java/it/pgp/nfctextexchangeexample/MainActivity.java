package it.pgp.nfctextexchangeexample;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.security.SecureRandom;
import java.util.zip.CRC32;
import java.util.zip.Checksum;

public class MainActivity extends Activity {

    Button launchExchange, updateCheckSums;
    TextView senderView,receiverView;

    SecureRandom r = new SecureRandom();
    Checksum cs = new CRC32();

    RadioGroup colorsSelector;
    RadioGroup gridSelector; // not used for now
    final int[] bitsPerCell = {1,2,3,4}; // 2,4,8,16 colors
    final int[] gridSizes = {8,10,12,14,16};
    int colorsSelectorIdx;
    int gridSelectorIdx;


    ViewGroup hvLayout;
    public static int hvWidth = -1, hvHeight = -1;

    public void updateSelectorsIndices() {
        colorsSelectorIdx = colorsSelector.indexOfChild(colorsSelector.findViewById(colorsSelector.getCheckedRadioButtonId()));
        gridSelectorIdx = gridSelector.indexOfChild(gridSelector.findViewById(gridSelector.getCheckedRadioButtonId()));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        launchExchange = findViewById(R.id.launchExchangeBtn);
        updateCheckSums = findViewById(R.id.updateChecksumsBtn);
        senderView = findViewById(R.id.senderChecksum);
        receiverView = findViewById(R.id.receiverChecksum);
        colorsSelector = findViewById(R.id.colorSelectorRadioGroup);
        gridSelector = findViewById(R.id.gridSelectorRadioGroup);

        launchExchange.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this,ExchangeActivity.class);
                // intent.putExtra(ExchangeActivity.senderDataTag, bToSend);
                startActivity(intent); // dummy request code
            }
        });

        updateCheckSums.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                computeAndShowCheckSums();
            }
        });

        colorsSelector.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                updateSelectorsIndices();
                if (StaticHelper.bReceived != null && StaticHelper.bToSend != null)
                    showVisualCommonChecksum(gridSizes[gridSelectorIdx],bitsPerCell[colorsSelectorIdx]);
            }
        });

        gridSelector.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                updateSelectorsIndices();
                if (StaticHelper.bReceived != null && StaticHelper.bToSend != null)
                    showVisualCommonChecksum(gridSizes[gridSelectorIdx],bitsPerCell[colorsSelectorIdx]);
            }
        });



    }

    @Override
    protected void onResume() {
        super.onResume();
        hvLayout = findViewById(R.id.hvLayout);

        updateSelectorsIndices();

        byte[] sent = getIntent().getByteArrayExtra("SENT");
        byte[] rec = getIntent().getByteArrayExtra("REC");

        if (sent != null && rec != null) {
            // activity re-run after data has been received
            StaticHelper.bToSend = sent;
            StaticHelper.bReceived = rec;
            computeAndShowCheckSums();
            showVisualCommonChecksum(gridSizes[gridSelectorIdx],bitsPerCell[colorsSelectorIdx]);
        }
        else {
            // activity run before data exchange
            StaticHelper.bReceived = null;
            StaticHelper.bToSend = new byte[StaticHelper.SIZE];
            r.nextBytes(StaticHelper.bToSend);
            computeAndShowCheckSums();
        }
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hvWidth == -1) hvWidth = hvLayout.getWidth();
        if (hvHeight == -1) hvHeight = hvLayout.getHeight();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        StaticHelper.bToSend = null;
        StaticHelper.bReceived = null;
        hvLayout = null;
    }

    private void showVisualCommonChecksum(int gridSize, int bitsPerCell) {
        try {
            if (hvWidth == -1 || hvHeight == -1) throw new IOException(); // very very very coherent ;D
            byte[] common = StaticHelper.orderedByteArrayConcat(StaticHelper.bReceived,StaticHelper.bToSend);
            HashView hv = new HashView(getApplicationContext(),common,gridSize,bitsPerCell,hvWidth,hvHeight);
            hv.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
            hvLayout.removeAllViews();
            hvLayout.addView(hv);
        } catch (IOException e) {
            Toast.makeText(getApplicationContext(),"IOException or screen size not ready",Toast.LENGTH_SHORT).show();
        }
    }

    public void computeAndShowCheckSums() {
        if (StaticHelper.bToSend != null) {
            cs.reset();
            cs.update(StaticHelper.bToSend,0,StaticHelper.bToSend.length);
            senderView.setText(Long.toHexString(cs.getValue()));
        }
        else {
            senderView.setText("null");
        }
        if (StaticHelper.bReceived != null) {
            cs.reset();
            cs.update(StaticHelper.bReceived,0,StaticHelper.bReceived.length);
            receiverView.setText(Long.toHexString(cs.getValue()));
        }
        else {
            receiverView.setText("null");
        }
    }
}
