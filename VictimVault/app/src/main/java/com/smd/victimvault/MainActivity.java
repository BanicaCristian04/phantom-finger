package com.smd.victimvault;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;
import androidx.annotation.Nullable;
public class MainActivity extends AppCompatActivity {

    private RecyclerView rvAccounts;
    AccountAdapter adapter;
    Handler handler = new Handler(Looper.getMainLooper());
    List<TotpAccount> accounts = new ArrayList<>();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        rvAccounts = findViewById(R.id.rv_accounts);
        rvAccounts.setLayoutManager(new LinearLayoutManager(this));

        accounts.add(new TotpAccount("Google", "alex.munteanu@gmail.com"));
        accounts.add(new TotpAccount("GitHub", "alex-mdev"));
        accounts.add(new TotpAccount("Binance", "amunteanu_trade"));
        accounts.add(new TotpAccount("Microsoft", "a.munteanu@outlook.com"));
        accounts.add(new TotpAccount("Amazon AWS", "alexm-cloud"));
        accounts.add(new TotpAccount("Dropbox", "alex.m.backup@gmail.com"));

        adapter = new AccountAdapter(accounts);
        rvAccounts.setAdapter(adapter);

        startCountdown();
    }

    private void startCountdown() {
        handler.post(new Runnable() {
            @Override
            public void run() {
                long currentSecond = System.currentTimeMillis() / 1000;
                int secondsLeft = (int) (30 - (currentSecond % 30));

                if (secondsLeft == 30) {
                    // Time is up! Generate new codes.
                    for (TotpAccount account : accounts) {
                        account.regenerateCode();
                    }
                    // Reset the timer visually
                    adapter.updateProgress(30);
                    // Do a full redraw ONLY when the numbers actually change
                    adapter.notifyItemRangeChanged(0, accounts.size());
                } else {
                    // Otherwise, just tick down the rings smoothly
                    adapter.updateProgress(secondsLeft);
                }

                handler.postDelayed(this, 1000);
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        handler.removeCallbacksAndMessages(null);
    }
}