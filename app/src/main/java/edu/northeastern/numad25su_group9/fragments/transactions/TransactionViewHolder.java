package edu.northeastern.numad25su_group9.fragments.transactions;

import android.content.Intent;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.Locale;

import edu.northeastern.numad25su_group9.R;
import edu.northeastern.numad25su_group9.activities.AddTransactionActivity;
import edu.northeastern.numad25su_group9.models.Transaction;

public class TransactionViewHolder extends RecyclerView.ViewHolder {

    private final ImageView categoryImageView;
    private final TextView descriptionTextView;
    private final TextView dateTextView;
    private final TextView amountTextView;
    private final CardView cardView;

    public TransactionViewHolder(@NonNull View itemView) {
        super(itemView);
        categoryImageView = itemView.findViewById(R.id.transaction_category);
        descriptionTextView = itemView.findViewById(R.id.transaction_description);
        dateTextView = itemView.findViewById(R.id.transaction_date);
        amountTextView = itemView.findViewById(R.id.transaction_amount);
        cardView = itemView.findViewById(R.id.transaction_card_view);
    }

    public void bind(Transaction transaction) {
        categoryImageView.setImageResource(getCategoryImage(transaction.getCategoryName()));
        descriptionTextView.setText(transaction.getDescription());
        dateTextView.setText(formatDate(transaction.getTransactionDateLocal()));
        amountTextView.setText(formatAmount(transaction.getAmount()));
        cardView.setOnClickListener(v -> {
            Intent intent = new Intent(itemView.getContext(), AddTransactionActivity.class);
            intent.putExtra(AddTransactionActivity.TRANSACTION_EXTRA_KEY, transaction);
            itemView.getContext().startActivity(intent);
        });
    }

    private int getCategoryImage(String category) {
        if (category.toLowerCase().contains("entertainment")) {
            return R.drawable.entertainment;
        }
        if (category.toLowerCase().contains("food")) {
            return R.drawable.food;
        }
        if (category.toLowerCase().contains("shopping")) {
            return R.drawable.shopping;
        }
        if (category.toLowerCase().contains("transport")) {
            return R.drawable.transport;
        }
        if (category.toLowerCase().contains("utilities")) {
            return R.drawable.utilities;
        }
        return R.drawable.question;
    }

    private String formatDate(LocalDateTime dateTime) {
        DateTimeFormatter formatter = DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM);
        return dateTime.format(formatter);
    }

    private String formatAmount(double amount) {
        // TODO: FORMAT AMOUNT
        return String.format(Locale.US, "$%,.2f", amount);
    }
}
