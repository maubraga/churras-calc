package com.churrascalc.app;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.Activity;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.text.InputFilter;
import android.text.InputType;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowInsets;
import android.view.animation.DecelerateInterpolator;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MainActivity extends Activity {
    private static final int BLACK = Color.rgb(10, 10, 10);
    private static final int PANEL = Color.rgb(26, 26, 26);
    private static final int ORANGE = Color.rgb(255, 122, 0);
    private static final int WHITE = Color.WHITE;
    private static final int MUTED = Color.rgb(170, 170, 170);
    private static final int LINE = Color.rgb(54, 54, 54);

    private final String[] proteins = {
            "Carne bovina", "Linguiça", "Frango", "Carne suína", "Coração"
    };
    private final boolean[] selectedProteins = {true, true, true, false, false};
    private final int[] hungerGrams = {200, 450, 650};
    private final String[] hungerLabels = {"LEVE", "NORMAL", "FOME ALTA"};
    private final String[] hungerSubtitles = {"200g por pessoa", "450g por pessoa", "650g por pessoa"};

    private FrameLayout root;
    private EditText adultInput;
    private EditText childInput;
    private LinearLayout hungerGroup;
    private GridLayout proteinGroup;
    private TextView yesButton;
    private TextView noButton;
    private int hungerIndex = 1;
    private boolean hasSides = true;

    @Override
    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        getWindow().setStatusBarColor(BLACK);
        getWindow().setNavigationBarColor(BLACK);

        root = new FrameLayout(this);
        root.setBackgroundColor(BLACK);
        root.setOnApplyWindowInsetsListener((view, insets) -> {
            view.setPadding(0, insets.getSystemWindowInsetTop(), 0, insets.getSystemWindowInsetBottom());
            return insets;
        });
        setContentView(root);
        root.post(() -> root.requestApplyInsets());
        showCalculator(false);
    }

    private void showCalculator(boolean animated) {
        LinearLayout content = new LinearLayout(this);
        content.setOrientation(LinearLayout.VERTICAL);
        content.setBackgroundColor(BLACK);
        content.setPadding(dp(16), dp(10), dp(16), dp(8));

        ImageView logo = new ImageView(this);
        logo.setImageResource(getResources().getIdentifier("churracalc_logo", "drawable", getPackageName()));
        logo.setAdjustViewBounds(true);
        logo.setScaleType(ImageView.ScaleType.FIT_CENTER);
        LinearLayout.LayoutParams logoParams = new LinearLayout.LayoutParams(-1, dp(120));
        logoParams.gravity = Gravity.CENTER_HORIZONTAL;
        logoParams.setMargins(0, 0, 0, dp(2));
        content.addView(logo, logoParams);

        TextView subtitle = text("Calcule a quantidade ideal para seu churrasco", 14, MUTED, false);
        subtitle.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams subtitleParams = matchWrap();
        subtitleParams.setMargins(0, 0, 0, dp(6));
        content.addView(subtitle, subtitleParams);

        content.addView(sectionTitle("Pessoas"));
        adultInput = numberInput("Adultos", "10");
        childInput = numberInput("Crianças", "2");
        LinearLayout peopleRow = new LinearLayout(this);
        peopleRow.setOrientation(LinearLayout.HORIZONTAL);
        peopleRow.addView(numberField("Adultos", "Consumo integral", adultInput), peopleWeightParams(true));
        peopleRow.addView(numberField("Crianças", "50% do adulto", childInput), peopleWeightParams(false));
        content.addView(peopleRow, matchWrap());

        content.addView(sectionTitle("Nível de fome"));
        hungerGroup = new LinearLayout(this);
        hungerGroup.setOrientation(LinearLayout.HORIZONTAL);
        for (int i = 0; i < hungerLabels.length; i++) {
            final int index = i;
            TextView option = optionButton(hungerLabels[i], hungerSubtitles[i], index == hungerIndex);
            option.setOnClickListener(v -> {
                hungerIndex = index;
                refreshHungerButtons();
            });
            hungerGroup.addView(option, compactWeightParams(1, i < hungerLabels.length - 1));
        }
        content.addView(hungerGroup, matchWrap());

        content.addView(sectionTitle("Acompanhamentos"));
        LinearLayout sidesRow = new LinearLayout(this);
        sidesRow.setOrientation(LinearLayout.HORIZONTAL);
        sidesRow.setGravity(Gravity.CENTER_VERTICAL);
        TextView sidesLabel = label("Vai ter acompanhamentos?");
        sidesRow.addView(sidesLabel, new LinearLayout.LayoutParams(0, dp(42), 1.25f));
        yesButton = switchButton("SIM", hasSides);
        noButton = switchButton("NÃO", !hasSides);
        yesButton.setOnClickListener(v -> {
            hasSides = true;
            refreshSideButtons();
        });
        noButton.setOnClickListener(v -> {
            hasSides = false;
            refreshSideButtons();
        });
        sidesRow.addView(yesButton, compactWeightParams(0.8f, true));
        sidesRow.addView(noButton, compactWeightParams(0.8f, false));
        content.addView(sidesRow, matchWrap());

        content.addView(sectionTitle("Selecione as proteínas"));
        proteinGroup = new GridLayout(this);
        proteinGroup.setColumnCount(2);
        refreshProteinRows();
        content.addView(proteinGroup, matchWrap());

        View buttonSpacer = new View(this);
        content.addView(buttonSpacer, new LinearLayout.LayoutParams(-1, 0, 1));

        TextView calc = actionButton("CALCULAR CHURRASCO");
        calc.setOnClickListener(v -> showResult(calculate(), true));
        LinearLayout.LayoutParams calcParams = new LinearLayout.LayoutParams(-1, dp(50));
        calcParams.setMargins(0, dp(6), 0, 0);
        content.addView(calc, calcParams);

        swap(content, animated);
    }

    private Result calculate() {
        int adults = parse(adultInput.getText().toString());
        int children = parse(childInput.getText().toString());
        int grams = hungerGrams[hungerIndex];
        double totalKg = ((adults * grams) + (children * grams * 0.5)) / 1000.0;
        if (hasSides) {
            totalKg *= 0.85;
        }

        List<Integer> selected = new ArrayList<>();
        for (int i = 0; i < selectedProteins.length; i++) {
            if (selectedProteins[i]) {
                selected.add(i);
            }
        }
        if (selected.isEmpty()) {
            selected.add(0);
        }

        double[][] distributions = {
                {1.0},
                {0.5, 0.5},
                {0.45, 0.30, 0.25},
                {0.40, 0.25, 0.20, 0.15},
                {0.35, 0.20, 0.18, 0.15, 0.12}
        };
        double[] percentages = distributions[selected.size() - 1];
        Result result = new Result();
        result.totalKg = totalKg;
        for (int i = 0; i < selected.size(); i++) {
            int proteinIndex = selected.get(i);
            result.items.add(new ResultItem(proteins[proteinIndex], totalKg * percentages[i], percentages[i]));
        }
        return result;
    }

    private void showResult(Result result, boolean animated) {
        LinearLayout content = new LinearLayout(this);
        content.setOrientation(LinearLayout.VERTICAL);
        content.setBackgroundColor(BLACK);
        content.setPadding(dp(18), dp(20), dp(18), dp(18));

        TextView eyebrow = text("TOTAL DE CARNE", 13, MUTED, true);
        eyebrow.setGravity(Gravity.CENTER);
        content.addView(eyebrow, matchWrap());

        TextView total = text(formatTotal(result.totalKg), 44, WHITE, true);
        total.setGravity(Gravity.CENTER);
        total.setLetterSpacing(0.02f);
        LinearLayout.LayoutParams totalParams = matchWrap();
        totalParams.setMargins(0, dp(4), 0, dp(12));
        content.addView(total, totalParams);

        for (ResultItem item : result.items) {
            content.addView(resultRow(item), matchWrap());
        }

        TextView again = actionButton("CALCULAR NOVAMENTE");
        again.setOnClickListener(v -> showCalculator(true));
        LinearLayout.LayoutParams againParams = new LinearLayout.LayoutParams(-1, dp(50));
        againParams.setMargins(0, dp(14), 0, 0);
        content.addView(again, againParams);

        swap(content, animated);
    }

    private View resultRow(ResultItem item) {
        LinearLayout box = new LinearLayout(this);
        box.setOrientation(LinearLayout.VERTICAL);
        box.setBackgroundColor(BLACK);
        box.setPadding(0, dp(8), 0, dp(8));

        View dividerTop = new View(this);
        dividerTop.setBackgroundColor(LINE);
        box.addView(dividerTop, new LinearLayout.LayoutParams(-1, dp(1)));

        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setPadding(0, dp(7), 0, dp(7));

        TextView name = text(item.name, 16, WHITE, false);
        TextView kg = text(formatKg(item.kg), 18, WHITE, true);
        kg.setGravity(Gravity.END);
        row.addView(name, weightParams(1));
        row.addView(kg, weightParams(1));
        box.addView(row, matchWrap());

        BarView bar = new BarView(this, (float) item.percent);
        box.addView(bar, new LinearLayout.LayoutParams(-1, dp(4)));
        return box;
    }

    private void refreshHungerButtons() {
        for (int i = 0; i < hungerGroup.getChildCount(); i++) {
            TextView view = (TextView) hungerGroup.getChildAt(i);
            styleOption(view, i == hungerIndex);
        }
    }

    private void refreshSideButtons() {
        styleSwitch(yesButton, hasSides);
        styleSwitch(noButton, !hasSides);
    }

    private void refreshProteinRows() {
        proteinGroup.removeAllViews();
        for (int i = 0; i < proteins.length; i++) {
            final int index = i;
            TextView row = text((selectedProteins[i] ? "■  " : "□  ") + proteins[i], 14, WHITE, false);
            row.setGravity(Gravity.CENTER_VERTICAL);
            row.setPadding(dp(10), 0, dp(8), 0);
            row.setBackground(rect(PANEL, LINE, 1));
            row.setOnClickListener(v -> {
                selectedProteins[index] = !selectedProteins[index];
                refreshProteinRows();
            });
            proteinGroup.addView(row, proteinCellParams(i));
        }
    }

    private TextView optionButton(String title, String subtitle, boolean selected) {
        TextView view = text(title + "\n" + subtitle, 12, WHITE, true);
        view.setGravity(Gravity.CENTER);
        view.setPadding(dp(5), 0, dp(5), 0);
        styleOption(view, selected);
        return view;
    }

    private void styleOption(TextView view, boolean selected) {
        view.setTextColor(selected ? BLACK : WHITE);
        view.setBackground(rect(selected ? ORANGE : PANEL, selected ? ORANGE : LINE, 1));
    }

    private TextView switchButton(String label, boolean selected) {
        TextView button = text(label, 15, selected ? BLACK : WHITE, true);
        button.setGravity(Gravity.CENTER);
        styleSwitch(button, selected);
        return button;
    }

    private void styleSwitch(TextView view, boolean selected) {
        view.setTextColor(selected ? BLACK : WHITE);
        view.setBackground(rect(selected ? ORANGE : PANEL, selected ? ORANGE : LINE, 1));
    }

    private EditText numberInput(String hint, String value) {
        EditText input = new EditText(this);
        input.setText(value);
        input.setHint(hint);
        input.setTextColor(WHITE);
        input.setHintTextColor(MUTED);
        input.setTextSize(16);
        input.setSingleLine(true);
        input.setSelectAllOnFocus(true);
        input.setInputType(InputType.TYPE_CLASS_NUMBER);
        input.setImeOptions(EditorInfo.IME_ACTION_NEXT);
        input.setFilters(new InputFilter[]{new InputFilter.LengthFilter(3)});
        input.setPadding(dp(10), 0, dp(10), 0);
        input.setBackground(rect(PANEL, LINE, 1));
        return input;
    }

    private View numberField(String title, String description, EditText input) {
        LinearLayout box = new LinearLayout(this);
        box.setOrientation(LinearLayout.VERTICAL);
        TextView titleView = text(title, 14, WHITE, true);
        TextView descriptionView = text(description, 12, MUTED, false);
        titleView.setIncludeFontPadding(false);
        descriptionView.setIncludeFontPadding(false);
        LinearLayout.LayoutParams descriptionParams = matchWrap();
        descriptionParams.setMargins(0, 0, 0, dp(3));

        box.addView(titleView, matchWrap());
        box.addView(descriptionView, descriptionParams);
        box.addView(input, new LinearLayout.LayoutParams(-1, dp(40)));
        return box;
    }

    private TextView sectionTitle(String value) {
        TextView view = text(value, 12, ORANGE, true);
        view.setAllCaps(true);
        LinearLayout.LayoutParams params = matchWrap();
        params.setMargins(0, dp(8), 0, dp(4));
        view.setLayoutParams(params);
        return view;
    }

    private TextView label(String value) {
        TextView view = text(value, 16, WHITE, false);
        view.setGravity(Gravity.CENTER_VERTICAL);
        view.setPadding(0, 0, dp(8), 0);
        return view;
    }

    private TextView actionButton(String value) {
        TextView button = text(value, 16, WHITE, true);
        button.setGravity(Gravity.CENTER);
        button.setBackground(rect(ORANGE, ORANGE, 0));
        return button;
    }

    private TextView text(String value, int sp, int color, boolean bold) {
        TextView view = new TextView(this);
        view.setText(value);
        view.setTextColor(color);
        view.setTextSize(sp);
        view.setIncludeFontPadding(true);
        view.setFontFeatureSettings("kern");
        view.setTypeface(Typeface.create("sans-serif-condensed", bold ? Typeface.BOLD : Typeface.NORMAL));
        return view;
    }

    private GradientDrawable rect(int fill, int stroke, int strokeDp) {
        GradientDrawable drawable = new GradientDrawable();
        drawable.setShape(GradientDrawable.RECTANGLE);
        drawable.setColor(fill);
        drawable.setCornerRadius(0);
        if (strokeDp > 0) {
            drawable.setStroke(dp(strokeDp), stroke);
        }
        return drawable;
    }

    private void swap(View next, boolean animated) {
        if (!animated || root.getChildCount() == 0) {
            root.removeAllViews();
            root.addView(next, new FrameLayout.LayoutParams(-1, -1));
            return;
        }
        View old = root.getChildAt(0);
        next.setAlpha(0f);
        next.setTranslationX(dp(18));
        root.addView(next, new FrameLayout.LayoutParams(-1, -1));
        next.animate().alpha(1f).translationX(0).setDuration(180)
                .setInterpolator(new DecelerateInterpolator()).start();
        old.animate().alpha(0f).translationX(-dp(18)).setDuration(160)
                .setInterpolator(new DecelerateInterpolator())
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        root.removeView(old);
                    }
                }).start();
    }

    private int parse(String value) {
        try {
            return Math.max(0, Integer.parseInt(value.trim()));
        } catch (NumberFormatException exception) {
            return 0;
        }
    }

    private String formatTotal(double kg) {
        return oneDecimal().format(kg).toUpperCase(Locale.ROOT) + " KG";
    }

    private String formatKg(double kg) {
        return twoDecimals().format(kg) + " kg";
    }

    private DecimalFormat oneDecimal() {
        return new DecimalFormat("0.0", symbols());
    }

    private DecimalFormat twoDecimals() {
        return new DecimalFormat("0.00", symbols());
    }

    private DecimalFormatSymbols symbols() {
        DecimalFormatSymbols symbols = new DecimalFormatSymbols(new Locale("pt", "BR"));
        symbols.setDecimalSeparator(',');
        return symbols;
    }

    private LinearLayout.LayoutParams matchWrap() {
        return new LinearLayout.LayoutParams(-1, -2);
    }

    private LinearLayout.LayoutParams blockParams() {
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(-1, dp(56));
        params.setMargins(0, 0, 0, dp(10));
        return params;
    }

    private LinearLayout.LayoutParams compactWeightParams(float weight, boolean addRightMargin) {
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(0, dp(42), weight);
        params.setMargins(0, 0, addRightMargin ? dp(6) : 0, 0);
        return params;
    }

    private LinearLayout.LayoutParams peopleWeightParams(boolean addRightMargin) {
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(0, dp(78), 1);
        params.setMargins(0, 0, addRightMargin ? dp(6) : 0, 0);
        return params;
    }

    private GridLayout.LayoutParams proteinCellParams(int index) {
        GridLayout.LayoutParams params = new GridLayout.LayoutParams();
        params.width = 0;
        params.height = dp(38);
        params.columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f);
        params.setMargins(0, 0, index % 2 == 0 ? dp(6) : 0, dp(6));
        return params;
    }

    private LinearLayout.LayoutParams weightParams(float weight) {
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(0, dp(48), weight);
        params.setMargins(0, 0, dp(8), 0);
        return params;
    }

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }

    private static class Result {
        double totalKg;
        final List<ResultItem> items = new ArrayList<>();
    }

    private static class ResultItem {
        final String name;
        final double kg;
        final double percent;

        ResultItem(String name, double kg, double percent) {
            this.name = name;
            this.kg = kg;
            this.percent = percent;
        }
    }

    public static class BarView extends View {
        private final Paint track = new Paint(Paint.ANTI_ALIAS_FLAG);
        private final Paint fill = new Paint(Paint.ANTI_ALIAS_FLAG);
        private final float percent;

        public BarView(android.content.Context context, float percent) {
            super(context);
            this.percent = percent;
            track.setColor(LINE);
            fill.setColor(ORANGE);
        }

        @Override
        protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);
            canvas.drawRect(0, 0, getWidth(), getHeight(), track);
            canvas.drawRect(0, 0, getWidth() * percent, getHeight(), fill);
        }
    }

}
