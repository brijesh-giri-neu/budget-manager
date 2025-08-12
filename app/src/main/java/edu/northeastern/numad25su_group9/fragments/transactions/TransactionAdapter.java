package edu.northeastern.numad25su_group9.fragments.transactions;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;
import java.util.stream.Collectors;

import edu.northeastern.numad25su_group9.R;
import edu.northeastern.numad25su_group9.models.Transaction;

public class TransactionAdapter extends RecyclerView.Adapter<TransactionViewHolder> {

    private final List<Transaction> transactionList;
    private final Context context;

    public TransactionAdapter(List<Transaction> transactionList, Context context) {
        this.transactionList = transactionList.stream().sorted((first, second) -> second.getTransactionDateLocal().compareTo(first.getTransactionDateLocal())).collect(Collectors.toList());
        this.context = context;
    }

    @NonNull
    @Override
    public TransactionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new TransactionViewHolder(LayoutInflater.from(context).inflate(R.layout.view_holder_transaction, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull TransactionViewHolder holder, int position) {
        holder.bind(transactionList.get(position));
    }

    @Override
    public int getItemCount() {
        return transactionList.size();
    }

    public void updateTransactions(List<Transaction> transactions) {
        if (transactions == null) {
            Log.e("TransactionAdapter", "Transactions list is null");
            return;
        }
        List<Transaction> sortedTransactions = transactions.stream().sorted((first, second) -> second.getTransactionDateLocal().compareTo(first.getTransactionDateLocal())).collect(Collectors.toList());
        transactionList.clear();
        transactionList.addAll(sortedTransactions);
        notifyDataSetChanged();
    }
}
