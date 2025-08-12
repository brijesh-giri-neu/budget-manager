package edu.northeastern.numad25su_group9.repositories;

import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;

import java.util.Map;

import edu.northeastern.numad25su_group9.models.Budget;
import edu.northeastern.numad25su_group9.utils.DateUtil;

/**
 * Maintain the budgetId constraint here.
 */
public class BudgetRepository extends BaseRepository {
    public BudgetRepository(String userId) {
        super(FirebaseDatabase.getInstance().getReference("budgets").child(userId));
    }

    public Task<Void> addBudget(Budget budget) {
        Map<String, Object> map = budget.toMap();
        map.put("createdAt", ServerValue.TIMESTAMP);
        map.put("updatedAt", ServerValue.TIMESTAMP);

        String budgetId = getDateKey(budget);
        return set(budgetId, map);
    }

    public Task<Void> updateBudget(Budget budget) {
        Map<String, Object> map = budget.toMap();
        map.put("updatedAt", ServerValue.TIMESTAMP);

        String budgetId = getDateKey(budget);
        return update(budgetId, map);
    }

    public Task<Void> deleteBudget(int year, int month) {
        String budgetId = DateUtil.toDateKey(year, month);
        return delete(budgetId);
    }
    public Task<DataSnapshot> getBudgetForMonth(int year, int month) {
        String budgetId = DateUtil.toDateKey(year, month);
        return get(budgetId);
    }
    public Task<DataSnapshot> getAllBudgets() { return getAll(); }

    public Task<DataSnapshot> getBudgetsByDateRange(long startMillis, long endMillis) {
        return ref.orderByChild("monthUtcTs")
                .startAt(startMillis)
                .endAt(endMillis)
                .get();
    }

    public Task<DataSnapshot> getLatestBudget() {
        return ref.orderByChild("monthUtcTs")
                .limitToLast(1)
                .get();
    }

    /**
     * Maintain the budgetId constraint here.
     */
    private String getDateKey(Budget budget) {
        return DateUtil.toDateKey(budget.getMonthUtcTs());
    }
}
