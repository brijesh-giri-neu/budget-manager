package edu.northeastern.numad25su_group9.fragments.spending;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.github.AAChartModel.AAChartCore.AAChartCreator.AAChartModel;
import com.github.AAChartModel.AAChartCore.AAChartCreator.AASeriesElement;
import com.github.AAChartModel.AAChartCore.AAChartEnum.AAChartLineDashStyleType;
import com.github.AAChartModel.AAChartCore.AAChartEnum.AAChartType;
import com.google.android.material.color.MaterialColors;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import edu.northeastern.numad25su_group9.databinding.FragmentSpendingBinding;
import edu.northeastern.numad25su_group9.models.Budget;
import edu.northeastern.numad25su_group9.models.MonthlySpendingSummary;
import edu.northeastern.numad25su_group9.models.Spending;
import edu.northeastern.numad25su_group9.models.Transaction;
import edu.northeastern.numad25su_group9.services.BudgetService;
import edu.northeastern.numad25su_group9.services.SpendingService;
import edu.northeastern.numad25su_group9.services.TransactionService;

public class SpendingFragment extends Fragment {

    public static final String TAG = "SpendingFragment";

    private FragmentSpendingBinding binding;

    public SpendingFragment() {
        // Required empty public constructor
        new SpendingService().getSpendingByMonth(2025, new SpendingService.MonthlySpendingsCallback() {
            @Override
            public void onSuccess(List<MonthlySpendingSummary> list) {

            }

            @Override
            public void onError(Exception e) {

            }
        });
    }

    public static SpendingFragment newInstance() {
        return new SpendingFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentSpendingBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        makeLineChart(LocalDate.now());

        makePieChart(LocalDate.now());
    }

    private void makePieChart(LocalDate monthYear) {
        Log.d(TAG, "Making pie chart");
        SpendingService spendingService = new SpendingService();
        spendingService.getSpendingsForMonthByCategory(monthYear.getYear(), monthYear.getMonthValue(), new SpendingService.SpendingsCallback() {
            @Override
            public void onSuccess(List<Spending> list) {
                if (!isAdded() || binding == null) {
                    Log.w(TAG, "Fragment is not attached");
                    return;
                }

                if (list.isEmpty()) {
                    binding.spendingPieCard.setVisibility(View.INVISIBLE);
                }

                Log.d(TAG, String.format("Success getting %d spendings", list.size()));

                Object[][] pieData = list.stream()
                        .map(spending -> new Object[]{spending.getCategoryName(), spending.getAmount()})
                        .toArray(Object[][]::new);

                int backgroundThemeColor = MaterialColors.getColor(binding.getRoot(), com.google.android.material.R.attr.colorSurface);
                String hexColor = String.format("#%06X", (0xFFFFFF & backgroundThemeColor));

                AAChartModel spendingCategoryPieChartModel = new AAChartModel()
                        .chartType(AAChartType.Pie)
                        .title("Spending by Category")
                        .backgroundColor(hexColor)
                        .dataLabelsEnabled(true)
                        .series(new AASeriesElement[]{new AASeriesElement().name("Spending").data(pieData)});
                binding.spendingCategoryPieChart.aa_drawChartWithChartModel(spendingCategoryPieChartModel);
            }

            @Override
            public void onError(Exception e) {
                Log.e(TAG, "Error getting spendings", e);
                Toast.makeText(getActivity(), "Error getting spendings", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void makeLineChart(LocalDate monthYear) {

        int backgroundThemeColor = MaterialColors.getColor(binding.getRoot(), com.google.android.material.R.attr.colorSurface);
        String hexColor = String.format("#%06X", (0xFFFFFF & backgroundThemeColor));

        AAChartModel spendingLineChartModel = new AAChartModel()
                .chartType(AAChartType.Area)
                .xAxisLabelsEnabled(false)
                .yAxisLabelsEnabled(false)
                .dataLabelsEnabled(false)
                .yAxisGridLineWidth(0f)
                .markerRadius(0)
                .backgroundColor(hexColor);

        int daysInMonth = monthYear.getMonth().length(monthYear.isLeapYear());
        String[] categories = new String[daysInMonth];
        for (int i = 0; i < daysInMonth; i++) {
            categories[i] = String.valueOf(i + 1);
        }
        spendingLineChartModel.categories(categories);

        BudgetService budgetService = new BudgetService();
        budgetService.getBudgetForMonth(monthYear.getYear(), monthYear.getMonthValue(), new BudgetService.BudgetCallback() {

            @Override
            public void onSuccess(Budget budget) {
                Double[] budgetArray = new Double[monthYear.getMonth().length(monthYear.isLeapYear())];
                Arrays.fill(budgetArray, budget.getAmount());
                AASeriesElement budgetElement = new AASeriesElement()
                        .name("Budget")
                        .allowPointSelect(false)
                        .color("#808080")
                        .fillOpacity(0.1)
                        .dashStyle(AAChartLineDashStyleType.Dash)
                        .data(budgetArray);
                importLineChartTransactionData(monthYear, spendingLineChartModel, budgetElement);
            }

            @Override
            public void onError(Exception e) {
                Log.e(TAG, "Error getting budget", e);
                Toast.makeText(getActivity(), "Error getting budget", Toast.LENGTH_SHORT).show();
                importLineChartTransactionData(monthYear, spendingLineChartModel, null);
            }
        });
    }

    private void importLineChartTransactionData(LocalDate date, AAChartModel spendingLineChartModel, AASeriesElement budgetSeries) {
        TransactionService transactionService = new TransactionService();
        transactionService.getTransactionsByMonth(date.getYear(), date.getMonthValue(), new TransactionService.TransactionsCallback() {

            @Override
            public void onSuccess(List<Transaction> transactions) {

                int daysInMonth = date.getMonth().length(date.isLeapYear());

                Double[] spendingArray = new Double[daysInMonth];
                Arrays.fill(spendingArray, 0.0); // Initialize with 0.0
                for (Transaction transaction : transactions) {
                    // Round to 2 decimal places
                    double roundedAmount = Math.round(transaction.getAmount() * 100.0) / 100.0;
                    spendingArray[transaction.getTransactionDateLocal().getDayOfMonth() - 1] += roundedAmount;
                }
                for (int i = 1; i < spendingArray.length; i++) {
                    // Round to 2 decimal places
                    spendingArray[i] = Math.round((spendingArray[i] + spendingArray[i-1]) * 100.0) / 100.0;
                }

                if (LocalDate.now().getMonth() == date.getMonth() && LocalDate.now().getYear() == date.getYear()) {
                    spendingArray = Arrays.copyOfRange(spendingArray, 0, LocalDate.now().getDayOfMonth());
                }

                AASeriesElement thisMonthSpending = new AASeriesElement()
                        .name("This Month")
                        .data(spendingArray);

                if (budgetSeries == null) {
                    spendingLineChartModel.series(new AASeriesElement[]{
                            thisMonthSpending
                    });
                } else {
                    spendingLineChartModel.series(new AASeriesElement[]{
                            thisMonthSpending,
                            budgetSeries
                    });
                }

                if (binding != null) {
                    binding.spendingLineSpendThisMonth.setText(String.format(Locale.US, "$%.2f", spendingArray[spendingArray.length - 1]));
                    binding.spendingChart.aa_drawChartWithChartModel(spendingLineChartModel);
                }
            }

            @Override
            public void onError(Exception e) {
                Log.e(TAG, "Error getting transactions", e);
                Toast.makeText(getActivity(), "Error getting transactions", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

}