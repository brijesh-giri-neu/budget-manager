package edu.northeastern.numad25su_group9.services;

import android.util.Log;

import com.google.firebase.database.DataSnapshot;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import edu.northeastern.numad25su_group9.models.MonthlySpendingSummary;
import edu.northeastern.numad25su_group9.models.Spending;
import edu.northeastern.numad25su_group9.models.Transaction;
import edu.northeastern.numad25su_group9.repositories.SpendingRepository;

public class SpendingService {
    private static final String TAG = "SpendingService";

    public interface OperationCallback { void onSuccess(); void onError(Exception e); }
    public interface TotalSpendingCallback { void onSuccess(double total); void onError(Exception e); }
    public interface SpendingsCallback { void onSuccess(List<Spending> list); void onError(Exception e); }
    public interface MonthlySpendingsCallback { void onSuccess(List<MonthlySpendingSummary> list); void onError(Exception e); }

    private final SpendingRepository repo;

    public SpendingService() {
        String userId = new AuthService().getCurrentUserId();
        this.repo = new SpendingRepository(userId);
    }

    public void addSpending(Spending b, OperationCallback cb) {
        repo.addSpending(b)
                .addOnSuccessListener(u -> cb.onSuccess())
                .addOnFailureListener(cb::onError);
    }

    public void updateSpending(Spending updates, OperationCallback cb) {
        repo.updateSpending(updates)
                .addOnSuccessListener(u -> cb.onSuccess())
                .addOnFailureListener(cb::onError);
    }

    public void deleteSpending(int year, int month, String categoryId, OperationCallback cb) {
        repo.deleteSpending(year, month, categoryId)
                .addOnSuccessListener(u -> cb.onSuccess())
                .addOnFailureListener(cb::onError);
    }

    /**
     * Get a list of {@link Spending} by category for a given month.
     *
     * @param year  int for year
     * @param month int for month
     * @param cb    callback for success or failure
     */
    public void getSpendingsForMonthByCategory(int year, int month, SpendingsCallback cb) {
        Log.d(TAG, String.format("Getting spendings for month %d-%d", year, month));
        repo.getSpendingsForMonth(year, month)
                .addOnSuccessListener(snap -> {
                    // Return default value
                    if (!snap.exists()) {
                        cb.onSuccess(new ArrayList<>());
                        return;
                    }
                    List<Spending> out = new ArrayList<>();
                    for (DataSnapshot child : snap.getChildren()) {
                        Spending b = child.getValue(Spending.class);
                        if (b != null) out.add(b);
                    }
                    cb.onSuccess(out);
                })
                .addOnFailureListener(cb::onError);
    }

    /**
     * Get total spending for a given month.
     */
    public void getTotalSpendingForMonth(int year, int month, TotalSpendingCallback cb) {
        repo.getSpendingsForMonth(year, month)
                .addOnSuccessListener(snap -> {
                    // Return default value
                    if (!snap.exists()) {
                        cb.onSuccess(0);
                        return;
                    }
                    double total = 0;
                    for (DataSnapshot child : snap.getChildren()) {
                        Spending b = child.getValue(Spending.class);
                        if (b != null) total += b.getAmount();
                    }
                    cb.onSuccess(total);
                })
                .addOnFailureListener(cb::onError);
    }

    /**
     * Fetches a list of {@link MonthlySpendingSummary} for a given year.
     * Aggregates data for all categories for a given month into a {@link MonthlySpendingSummary}
     * object.
     */
    public void getSpendingByMonth(int year, MonthlySpendingsCallback cb) {
        repo.getSpendingsForYear(year)
                .addOnSuccessListener(snap -> {
                    // Return default value
                    if (!snap.exists() || !snap.hasChildren()) {
                        cb.onSuccess(new ArrayList<>());
                        return;
                    }

                    // Map to store total spending for each month (1-12)
                    Map<Integer, Double> monthlyTotals = new HashMap<>();
                    // Iterate over the month keys.
                    for (DataSnapshot monthSnap : snap.getChildren()) {
                        String monthKey = monthSnap.getKey();
                        if (monthKey == null) continue;

                        int month = Integer.parseInt(
                                monthKey.substring(monthKey.length() - 2));
                        double totalForMonth = 0;

                        // Iterate over the categories within that month.
                        for (DataSnapshot categorySnap : monthSnap.getChildren()) {
                            if (!categorySnap.exists()) continue;
                            Spending spending = categorySnap.getValue(Spending.class);
                            if (spending != null) {
                                totalForMonth += spending.getAmount();
                            }
                        }
                        // Add to aggregate map
                        monthlyTotals.put(month, totalForMonth);
                    }

                    // Convert map to list
                    List<MonthlySpendingSummary> out = new ArrayList<>();
                    for (int i = 1; i <= 12; i++) {
                        Double total = monthlyTotals.getOrDefault(i, 0.0);
                        if (total == null) continue;
                        out.add(new MonthlySpendingSummary(i, year, total));
                    }

                    cb.onSuccess(out);
                })
                .addOnFailureListener(cb::onError);
    }

    /** Atomically add delta to the spending amount of a given category for the given month. */
    public void incrementSpend(Transaction txn, double delta, OperationCallback cb) {
        LocalDateTime utc = txn.getTransactionDateUtc();
        long monthStartUtcTs = LocalDateTime.of(utc.getYear(), utc.getMonthValue(), 1, 0, 0)
                .atZone(ZoneId.of("UTC")).toInstant().toEpochMilli();

        Spending s = Spending.builder()
                .setCategoryName(txn.getCategoryName())
                .setMonthDateUtcTs(monthStartUtcTs)
                .build();

        repo.incrementSpendingAmount(s, delta)
                .addOnSuccessListener(u -> cb.onSuccess())
                .addOnFailureListener(cb::onError);
    }
}
