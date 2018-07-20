package boozagame.mybitcoinapplication;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import org.bitcoinj.core.ECKey;
import org.bitcoinj.core.NetworkParameters;
import org.bitcoinj.core.listeners.DownloadProgressTracker;
import org.bitcoinj.kits.WalletAppKit;
import org.bitcoinj.params.MainNetParams;
import org.bitcoinj.params.TestNet3Params;
import org.bitcoinj.utils.BriefLogFormatter;
import org.bitcoinj.utils.Threading;
import org.bitcoinj.wallet.Wallet;

import java.io.File;
import java.util.Date;


public class MainActivity extends AppCompatActivity {

    /** logger **/
    private static final String TAG = MainActivity.class.getSimpleName();

    /** wallet location **/
    private File walletDir;

    /** use network to sync  Bitcoin network */
    private NetworkParameters parameters;

    /** wallet **/
    private WalletAppKit walletAppKit;

    /** Progress Dialog **/
    private ProgressDialog dialog;

    /** actual progress in number **/
    private int progressStatus = 0;

    /** alert dialog  builder **/
    private AlertDialog.Builder builder;

    /** alert dialog **/
    private AlertDialog alert;

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if ( dialog!=null && dialog.isShowing() ){
            dialog.cancel();
        }
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        // initialize progress dialog
        dialog = new ProgressDialog(MainActivity.this);
        dialog.setCancelable(true);
        dialog.setIndeterminate(false);
        dialog.setMessage(" Syncing with Bitcoin test network. This process will take couple of minutes (10 minutes or so). Be Patient. But it is ONE time only");
        dialog.show();

        //initialize Bitcoin thread
        final Handler handler = new Handler();
        Threading.USER_THREAD = handler::post;

        //
        //BriefLogFormatter.init();

        walletDir = getCacheDir();
        parameters = MainNetParams.get();//TestNet3Params.get();


        // initialize wallet
        walletAppKit = new WalletAppKit(parameters, walletDir, Constants.WALLET_NAME) {
            @Override
            protected void onSetupCompleted() {

                if (wallet().getImportedKeys().size() < 1) wallet().importKey(new ECKey());
//
                wallet().allowSpendingUnconfirmedTransactions();
                setupWalletListeners(wallet());

                Log.d(TAG, "My address = " + wallet().freshReceiveAddress());
                Log.d(TAG, "Current Block = " + wallet().getLastBlockSeenHash());
                Log.d(TAG, "Previous Block = " + wallet().currentChangeAddress());

                Log.d(TAG, " Block Time = " + wallet().getLastBlockSeenTime());

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        dialog.dismiss();

                        StringBuilder msg = new StringBuilder();
                        msg.append(" Sync Successfully with MainNet network \n\n")
                                .append("Current Wallet Address = ").append(wallet().freshReceiveAddress())
                                .append("\n\n").append("Current Block = ").append(wallet().getLastBlockSeenHash())
                                .append("\n\n").append(" Previous Block : ").append(wallet().currentChangeAddress())
                                .append("\n\n").append(" Time : ").append(wallet().getLastBlockSeenTime());

                        displayALert(msg.toString());
                    }
                });

            }
        };

        // download load listener
        walletAppKit.setDownloadListener(new DownloadProgressTracker() {
            @Override
            protected void progress(double pct, int blocksSoFar, Date date) {
                super.progress(pct, blocksSoFar, date);
                int percentage = (int) pct;

                dialog.setProgress(percentage);
                Log.d(TAG, "Progress..." + percentage);
            }

            @Override
            protected void doneDownload() {
                super.doneDownload();

                Log.d(TAG, "Download completed...");

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        dialog.dismiss();

                        StringBuilder msg = new StringBuilder();
                        msg.append(" Sync Successfully with MainNet network \n\n")
                                .append("Current Wallet Address = ").append(walletAppKit.wallet().freshReceiveAddress())
                                .append("\n\n").append("Current Block = ").append(walletAppKit.wallet().getLastBlockSeenHash())
                                .append("\n\n").append(" Time : ").append(walletAppKit.wallet().getLastBlockSeenTime());

                        displayALert(msg.toString());
                    }
                });
            }
        });

        // a non-blocking wallet initialization
        walletAppKit.setBlockingStartup(false);

        /// starts wallet app kit asynchronously so that we can monitor ;as
        walletAppKit.startAsync();


        builder = new AlertDialog.Builder(MainActivity.this);
        builder.setCancelable(false)
                .setPositiveButton("OK" , new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {

                    }
                });

        alert = builder.create();
        alert.hide();


    }

    /**
     * wallet listeners for transactions
     * @param wallet
     */
    private void setupWalletListeners(Wallet wallet) {

        /** provide implementation once you received money **/
        wallet.addCoinsReceivedEventListener((wallet1, tx, prevBalance, newBalance) -> {

        });

        /** provide implementation once you send money **/
        wallet.addCoinsSentEventListener((wallet12, tx, prevBalance, newBalance) -> {

        });
    }

    /**
     * display alert dialog with message
     * @param msg
     */
    private void displayALert(String msg) {
        alert.setMessage(msg);
        alert.show();

    }
}
