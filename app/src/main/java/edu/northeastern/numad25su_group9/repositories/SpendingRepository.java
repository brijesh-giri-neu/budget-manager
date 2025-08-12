package edu.northeastern.numad25su_group9.repositories;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.TaskCompletionSource;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.Transaction;

import java.util.HashMap;
import java.util.Map;

import edu.northeastern.numad25su_group9.models.Spending;
import edu.northeastern.numad25su_group9.services.CategoryService;
import edu.northeastern.numad25su_group9.utils.DateUtil;

/**
 * Maintain the spendingId constraint here.
 */
public class SpendingRepository extends BaseRepository {
    public SpendingRepository(String userId) {
        super(FirebaseDatabase.getInstance().getReference("spendings").child(userId));
    }

    public Task<Void> addSpending(Spending spending) {
        Map<String, Object> map = spending.toMap();
        map.put("createdAt", ServerValue.TIMESTAMP);
        map.put("updatedAt", ServerValue.TIMESTAMP);

        String dateKey = getDateKey(spending);
        String categoryId = CategoryService.getCategoryIdFromName(spending.getCategoryName());
        if (dateKey == null || categoryId == null) {
            TaskCompletionSource<Void> tcs = new TaskCompletionSource<>();
            tcs.setException(new IllegalArgumentException("Invalid category or date"));
            return tcs.getTask();
        }

        return ref.child(dateKey).child(categoryId).setValue(map);
    }

    public Task<Void> updateSpending(Spending spending) {
        Map<String, Object> map = spending.toMap();
        map.put("updatedAt", ServerValue.TIMESTAMP);

        String dateKey = getDateKey(spending);
        String categoryId = CategoryService.getCategoryIdFromName(spending.getCategoryName());
        if (dateKey == null || categoryId == null) {
            TaskCompletionSource<Void> tcs = new TaskCompletionSource<>();
            tcs.setException(new IllegalArgumentException("Invalid category or date"));
            return tcs.getTask();
        }

        return ref.child(dateKey).child(categoryId).updateChildren(map);
    }

    public Task<Void> deleteSpending(int year, int month, String categoryId) {
        String dateKey = DateUtil.toDateKey(year, month);

        if (dateKey == null || categoryId == null) {
            TaskCompletionSource<Void> tcs = new TaskCompletionSource<>();
            tcs.setException(new IllegalArgumentException("Invalid category or date"));
            return tcs.getTask();
        }
        return ref.child(dateKey).child(categoryId).removeValue();
    }

    public Task<DataSnapshot> getSpendingsForMonth(int year, int month) {
        String dateKey = DateUtil.toDateKey(year, month);

        if (dateKey == null) {
            TaskCompletionSource<DataSnapshot> tcs = new TaskCompletionSource<>();
            tcs.setException(new IllegalArgumentException("Invalid date"));
            return tcs.getTask();
        }
        return get(dateKey);
    }

    public Task<DataSnapshot> getSpendingsForYear(int year) {
        String startKey = DateUtil.toDateKey(year, 1);
        // Use a string that is one character past the last valid key for the year
        String endKey = DateUtil.toDateKey(year, 12);

        return ref.orderByKey().startAt(startKey).endAt(endKey).get();
    }

    /**
     * Atomically increments the spending amount for a given month/category,
     * creating the node if it doesn't exist.
     */
    public Task<Void> incrementSpendingAmount(Spending spending, double delta) {
        @NonNull final String dateKey = getDateKey(spending);    
        @NonNull final String categoryName = spending.getCategoryName();
        final long monthUtcTs = spending.getMonthUtcTs();

        String categoryId = CategoryService.getCategoryIdFromName(categoryName);
        if (dateKey == null || categoryId == null) {
            TaskCompletionSource<Void> tcs = new TaskCompletionSource<>();
            tcs.setException(new IllegalArgumentException("Category name or date is null"));
            return tcs.getTask();
        }

        final DatabaseReference categoryRef = ref.child(dateKey).child(categoryId);
        final TaskCompletionSource<Void> tcs = new TaskCompletionSource<>();

        categoryRef.runTransaction(new Transaction.Handler() {
            @NonNull
            @Override
            public Transaction.Result doTransaction(@NonNull MutableData currentData) {
                Object raw = currentData.getValue();
                Map<String, Object> cur;

                // Spending does not exist. Create new entry.
                if (raw == null) {
                    cur = new HashMap<>();
                    cur.put("categoryName", categoryName);
                    cur.put("monthUtcTs", monthUtcTs);
                    cur.put("amount", delta);
                    cur.put("createdAt", ServerValue.TIMESTAMP);
                    cur.put("updatedAt", ServerValue.TIMESTAMP);
                } else {
                    //noinspection unchecked
                    cur = (Map<String, Object>) raw;
                    Object updatedAmountObj = cur.get("amount");
                    double updatedAmount = (updatedAmountObj instanceof Number) ?
                            ((Number) updatedAmountObj).doubleValue() : 0.0;
                    updatedAmount += delta;

                    cur.put("amount", updatedAmount);
                    cur.put("updatedAt", ServerValue.TIMESTAMP);
                }

                currentData.setValue(cur);
                return Transaction.success(currentData);
            }

            @Override
            public void onComplete(DatabaseError error, boolean committed, DataSnapshot snapshot) {
                if (error != null) {
                    tcs.setException(error.toException());
                } else if (!committed) {
                    tcs.setException(new IllegalStateException("Increment not committed"));
                } else {
                    tcs.setResult(null);
                }
            }
        });

        return tcs.getTask();
    }

    private String getDateKey(Spending Spending) {
        return DateUtil.toDateKey(Spending.getMonthUtcTs());
    }
}
