package com.smd.victimvault;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.progressindicator.CircularProgressIndicator;
import java.util.List;

public class AccountAdapter extends RecyclerView.Adapter<AccountAdapter.ViewHolder> {

    private List<TotpAccount> accounts;
    private int currentProgress = 30;

    public AccountAdapter(@NonNull  List<TotpAccount> accounts) {
        this.accounts = accounts;
    }

    public void updateProgress(int progress) {
        this.currentProgress = progress;
        notifyItemRangeChanged(0, accounts.size(), "PROGRESS_UPDATE");
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_account, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position, @NonNull List<Object> payloads) {
        if (!payloads.isEmpty() && payloads.contains("PROGRESS_UPDATE")) {
            holder.pbCountdown.setProgressCompat(currentProgress, true);
        } else {
            onBindViewHolder(holder, position);
        }
    }
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        TotpAccount account = accounts.get(position);
        holder.tvService.setText(account.getServiceName());
        holder.tvUsername.setText(account.getUsername());
        holder.tvInitial.setText(String.valueOf(account.getServiceName().charAt(0)));

        String code = account.getCurrentCode();
        String formatted = code.substring(0, 3) + " " + code.substring(3);
        holder.tvCode.setText(formatted);

        holder.pbCountdown.setProgress(currentProgress);
    }

    @Override
    public int getItemCount() {
        return accounts.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvService;
        TextView tvUsername;
        TextView tvCode;
        TextView tvInitial;
        CircularProgressIndicator pbCountdown; // Added

        ViewHolder(View view) {
            super(view);
            tvService = view.findViewById(R.id.tv_service);
            tvUsername = view.findViewById(R.id.tv_username);
            tvCode = view.findViewById(R.id.tv_code);
            tvInitial = view.findViewById(R.id.tv_initial);
            pbCountdown = view.findViewById(R.id.pb_countdown); // Bound
        }
    }
}