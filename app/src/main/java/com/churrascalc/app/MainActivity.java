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
import android.view.Gravity;
import android.view.View;
import android.view.WindowInsets;
import android.view.animation.DecelerateInterpolator;
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
    private static final int PANEL_DARK = Color.rgb(18, 18, 18);
    private static final int ORANGE = Color.rgb(255, 122, 0);
    private static final int WHITE = Color.WHITE;
    private static final int MUTED = Color.rgb(178, 178, 178);
    private static final int LINE = Color.rgb(58, 58, 58);

    private final String[] proteins = {
            "Carne bovina", "Lingui\u00e7a", "Frango", "Carne su\u00edna", "Cora\u00e7\u00e3o"
    };
    private final boolean[] selectedProteins = {true, true, true, false, false};
    private final int[] hungerGrams = {200, 450, 650};
    private final String[] hungerLabels = {"LEVE", "NORMAL", "FOME ALTA"};

    private FrameLayout root;
    private TextView adultValue;
    private TextView childValue;
    private LinearLayout hungerGroup;
    private GridLayout proteinGroup;
    private TextView yesButton;
    private TextView noButton;
    private int adults = 10;
    private int children = 2;
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
        content.setPadding(dp(16), dp(8), dp(16), dp(8));

        ImageView logo = new ImageView(this);
        logo.setImageResource(getResources().getIdentifier("churracalc_logo_horizontal", "drawable", getPackageName()));
        logo.setAdjustViewBounds(true);
        logo.setScaleType(ImageView.ScaleType.FIT_CENTER);
        LinearLayout.LayoutParams logoParams = new LinearLayout.LayoutParams(-1, dp(100));
        logoParams.setMargins(0, 0, 0, dp(6));
        content.addView(logo, logoParams);

        LinearLayout peopleCard = sectionCard("\u25b3", "PESSOAS");
        LinearLayout peopleBox = new LinearLayout(this);
        peopleBox.setOrientation(LinearLayout.HORIZONTAL);
        peopleBox.setGravity(Gravity.CENTER);
        peopleBox.setPadding(dp(8), dp(6), dp(8), dp(6));
        peopleBox.setBackground(rect(PANEL_DARK, LINE, 1, 4));
        peopleBox.addView(counterPanel("ADULTOS", false), new LinearLayout.LayoutParams(0, dp(78), 1));
        View divider = new View(this);
        divider.setBackgroundColor(LINE);
        LinearLayout.LayoutParams dividerParams = new LinearLayout.LayoutParams(dp(1), dp(68));
        dividerParams.setMargins(dp(8), 0, dp(8), 0);
        peopleBox.addView(divider, dividerParams);
        peopleBox.addView(counterPanel("CRIAN\u00c7AS", true), new LinearLayout.LayoutParams(0, dp(78), 1));
        peopleCard.addView(peopleBox, matchWrap());
        content.addView(peopleCard, cardParams());

        LinearLayout hungerCard = sectionCard("\u2668", "N\u00cdVEL DE FOME");
        hungerGroup = new LinearLayout(this);
        hungerGroup.setOrientation(LinearLayout.HORIZONTAL);
        for (int i = 0; i < hungerLabels.length; i++) {
            final int index = i;
            View option = hungerOption(index);
            option.setOnClickListener(v -> {
                hungerIndex = index;
                refreshHungerButtons();
            });
            hungerGroup.addView(option, compactCardWeight(i < hungerLabels.length - 1));
        }
        hungerCard.addView(hungerGroup, matchWrap());
        content.addView(hungerCard, cardParams());

        LinearLayout sidesCard = sectionCard("\u26c4", "ACOMPANHAMENTOS");
        LinearLayout sidesRow = new LinearLayout(this);
        sidesRow.setOrientation(LinearLayout.HORIZONTAL);
        sidesRow.setGravity(Gravity.CENTER_VERTICAL);
        LinearLayout sidesText = new LinearLayout(this);
        sidesText.setOrientation(LinearLayout.VERTICAL);
        TextView question = text("Vai ter\nacompanhamentos?", 17, WHITE, true);
        TextView hint = text("Reduz 15% do consumo total", 11, MUTED, false);
        sidesText.addView(question, matchWrap());
        sidesText.addView(hint, matchWrap());
        sidesRow.addView(sidesText, new LinearLayout.LayoutParams(0, dp(48), 1));
        LinearLayout switchRow = new LinearLayout(this);
        switchRow.setOrientation(LinearLayout.HORIZONTAL);
        yesButton = switchButton("SIM", hasSides);
        noButton = switchButton("N\u00c3O", !hasSides);
        yesButton.setOnClickListener(v -> {
            hasSides = true;
            refreshSideButtons();
        });
        noButton.setOnClickListener(v -> {
            hasSides = false;
            refreshSideButtons();
        });
        switchRow.addView(yesButton, new LinearLayout.LayoutParams(0, dp(46), 1));
        switchRow.addView(noButton, new LinearLayout.LayoutParams(0, dp(46), 1));
        sidesRow.addView(switchRow, new LinearLayout.LayoutParams(0, dp(46), 1.05f));
        sidesCard.addView(sidesRow, matchWrap());
        content.addView(sidesCard, cardParams());

        LinearLayout proteinCard = sectionCard("\u25ce", "SELECIONE AS PROTE\u00cdNAS");
        proteinGroup = new GridLayout(this);
        proteinGroup.setColumnCount(2);
        refreshProteinRows();
        proteinCard.addView(proteinGroup, matchWrap());
        content.addView(proteinCard, cardParams());

        TextView calc = actionButton("\u25a3   CALCULAR CHURRASCO");
        calc.setOnClickListener(v -> showResult(calculate(), true));
        LinearLayout.LayoutParams calcParams = new LinearLayout.LayoutParams(-1, dp(50));
        calcParams.setMargins(0, 0, 0, 0);
        content.addView(calc, calcParams);

        swap(content, animated);
    }

    private Result calculate() {
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

    private LinearLayout sectionCard(String icon, String title) {
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setPadding(dp(10), dp(6), dp(10), dp(8));
        card.setBackground(rect(PANEL, LINE, 1, 6));

        LinearLayout header = new LinearLayout(this);
        header.setOrientation(LinearLayout.HORIZONTAL);
        header.setGravity(Gravity.CENTER_VERTICAL);
        TextView iconView = text(icon, 19, ORANGE, false);
        iconView.setGravity(Gravity.CENTER);
        header.addView(iconView, new LinearLayout.LayoutParams(dp(26), dp(24)));
        TextView titleView = text(title, 16, ORANGE, true);
        LinearLayout.LayoutParams titleParams = new LinearLayout.LayoutParams(-2, dp(25));
        titleParams.setMargins(dp(8), 0, 0, 0);
        header.addView(titleView, titleParams);
        LinearLayout.LayoutParams headerParams = matchWrap();
        headerParams.setMargins(0, 0, 0, dp(5));
        card.addView(header, headerParams);
        return card;
    }

    private View counterPanel(String title, boolean isChild) {
        LinearLayout panel = new LinearLayout(this);
        panel.setOrientation(LinearLayout.VERTICAL);
        panel.setGravity(Gravity.CENTER);

        TextView label = text(title, 13, WHITE, true);
        label.setGravity(Gravity.CENTER);
        panel.addView(label, matchWrap());

        TextView value = text(String.valueOf(isChild ? children : adults), 27, WHITE, true);
        value.setGravity(Gravity.CENTER);
        if (isChild) {
            childValue = value;
        } else {
            adultValue = value;
        }
        panel.addView(value, matchWrap());

        LinearLayout controls = new LinearLayout(this);
        controls.setGravity(Gravity.CENTER);
        TextView minus = stepButton("\u2212");
        TextView plus = stepButton("+");
        minus.setOnClickListener(v -> changePeople(isChild, -1));
        plus.setOnClickListener(v -> changePeople(isChild, 1));
        controls.addView(minus, new LinearLayout.LayoutParams(dp(42), dp(28)));
        TextView separator = text("|", 20, LINE, false);
        separator.setGravity(Gravity.CENTER);
        controls.addView(separator, new LinearLayout.LayoutParams(dp(20), dp(28)));
        controls.addView(plus, new LinearLayout.LayoutParams(dp(42), dp(28)));
        panel.addView(controls, matchWrap());

        if (isChild) {
            TextView hint = text("Crian\u00e7a: 50% do adulto", 9, MUTED, false);
            hint.setGravity(Gravity.CENTER);
            panel.addView(hint, matchWrap());
        }
        return panel;
    }

    private TextView stepButton(String label) {
        TextView button = text(label, 20, label.equals("+") ? ORANGE : WHITE, true);
        button.setGravity(Gravity.CENTER);
        button.setBackground(rect(PANEL, LINE, 1, 4));
        return button;
    }

    private void changePeople(boolean child, int delta) {
        if (child) {
            children = Math.max(0, Math.min(99, children + delta));
            childValue.setText(String.valueOf(children));
        } else {
            adults = Math.max(0, Math.min(99, adults + delta));
            adultValue.setText(String.valueOf(adults));
        }
    }

    private View hungerOption(int index) {
        LinearLayout option = new LinearLayout(this);
        option.setOrientation(LinearLayout.VERTICAL);
        option.setGravity(Gravity.CENTER);
        option.setPadding(dp(4), dp(5), dp(4), dp(5));

        TextView label = text(hungerLabels[index], 13, WHITE, true);
        label.setGravity(Gravity.CENTER);
        TextView grams = text(hungerGrams[index] + "g", 20, index == hungerIndex ? ORANGE : WHITE, true);
        grams.setGravity(Gravity.CENTER);
        TextView sub = text("por pessoa", 12, index == hungerIndex ? ORANGE : MUTED, false);
        sub.setGravity(Gravity.CENTER);
        label.setIncludeFontPadding(false);
        grams.setIncludeFontPadding(false);
        sub.setIncludeFontPadding(false);
        option.addView(label, matchWrap());
        option.addView(grams, matchWrap());
        option.addView(sub, matchWrap());
        styleHungerOption(option, index == hungerIndex);
        return option;
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
            LinearLayout option = (LinearLayout) hungerGroup.getChildAt(i);
            TextView grams = (TextView) option.getChildAt(1);
            TextView sub = (TextView) option.getChildAt(2);
            boolean selected = i == hungerIndex;
            grams.setTextColor(selected ? ORANGE : WHITE);
            sub.setTextColor(selected ? ORANGE : MUTED);
            styleHungerOption(option, selected);
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
            LinearLayout row = new LinearLayout(this);
            row.setOrientation(LinearLayout.HORIZONTAL);
            row.setGravity(Gravity.CENTER_VERTICAL);
            row.setPadding(dp(10), 0, dp(8), 0);
            row.setBackground(rect(PANEL_DARK, LINE, 1, 4));
            row.setOnClickListener(v -> {
                selectedProteins[index] = !selectedProteins[index];
                refreshProteinRows();
            });

            ProteinIconView icon = new ProteinIconView(this, i);
            row.addView(icon, new LinearLayout.LayoutParams(dp(32), -1));
            TextView name = text(proteins[i], 14, WHITE, false);
            row.addView(name, new LinearLayout.LayoutParams(0, -1, 1));
            TextView check = text(selectedProteins[i] ? "\u2713" : "", 20, BLACK, true);
            check.setGravity(Gravity.CENTER);
            check.setBackground(rect(selectedProteins[i] ? ORANGE : PANEL_DARK, selectedProteins[i] ? ORANGE : MUTED, 1, 0));
            row.addView(check, new LinearLayout.LayoutParams(dp(21), dp(21)));
            proteinGroup.addView(row, proteinCellParams(i));
        }
    }

    private TextView switchButton(String label, boolean selected) {
        TextView button = text(label, 16, selected ? WHITE : WHITE, true);
        button.setGravity(Gravity.CENTER);
        styleSwitch(button, selected);
        return button;
    }

    private void styleSwitch(TextView view, boolean selected) {
        view.setTextColor(WHITE);
        view.setBackground(rect(selected ? ORANGE : PANEL_DARK, selected ? ORANGE : LINE, 1, 0));
    }

    private void styleHungerOption(LinearLayout view, boolean selected) {
        view.setBackground(rect(PANEL_DARK, selected ? ORANGE : LINE, selected ? 2 : 1, 5));
    }

    private TextView actionButton(String value) {
        TextView button = text(value, 16, WHITE, true);
        button.setGravity(Gravity.CENTER);
        button.setBackground(rect(ORANGE, ORANGE, 0, 3));
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

    private GradientDrawable rect(int fill, int stroke, int strokeDp, int radiusDp) {
        GradientDrawable drawable = new GradientDrawable();
        drawable.setShape(GradientDrawable.RECTANGLE);
        drawable.setColor(fill);
        drawable.setCornerRadius(dp(radiusDp));
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

    private String formatTotal(double kg) {
        return oneDecimal().format(kg).toUpperCase(Locale.ROOT) + " KG";
    }

    private String formatKg(double kg) {
        return threeDecimals().format(kg) + " kg";
    }

    private DecimalFormat oneDecimal() {
        return new DecimalFormat("0.0", symbols());
    }

    private DecimalFormat threeDecimals() {
        return new DecimalFormat("0.000", symbols());
    }

    private DecimalFormatSymbols symbols() {
        DecimalFormatSymbols symbols = new DecimalFormatSymbols(new Locale("pt", "BR"));
        symbols.setDecimalSeparator(',');
        return symbols;
    }

    private LinearLayout.LayoutParams matchWrap() {
        return new LinearLayout.LayoutParams(-1, -2);
    }

    private LinearLayout.LayoutParams cardParams() {
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(-1, -2);
        params.setMargins(0, 0, 0, dp(6));
        return params;
    }

    private LinearLayout.LayoutParams compactCardWeight(boolean addRightMargin) {
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(0, dp(76), 1);
        params.setMargins(0, 0, addRightMargin ? dp(8) : 0, 0);
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

    public static class ProteinIconView extends View {
        private final int type;
        private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);

        public ProteinIconView(android.content.Context context, int type) {
            super(context);
            this.type = type;
            paint.setColor(WHITE);
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(2.2f);
            paint.setStrokeCap(Paint.Cap.SQUARE);
            paint.setStrokeJoin(Paint.Join.MITER);
        }

        @Override
        protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);
            float w = getWidth();
            float h = getHeight();
            float cx = w / 2f;
            float cy = h / 2f;
            if (type == 0) {
                drawCow(canvas, cx, cy);
            } else if (type == 1) {
                drawSausage(canvas, cx, cy);
            } else if (type == 2) {
                drawChicken(canvas, cx, cy);
            } else if (type == 3) {
                drawPig(canvas, cx, cy);
            } else {
                drawHeart(canvas, cx, cy);
            }
        }

        private void drawCow(Canvas canvas, float cx, float cy) {
            canvas.drawRect(cx - 10, cy - 6, cx + 8, cy + 6, paint);
            canvas.drawLine(cx - 6, cy + 6, cx - 6, cy + 11, paint);
            canvas.drawLine(cx + 5, cy + 6, cx + 5, cy + 11, paint);
            canvas.drawLine(cx + 8, cy - 3, cx + 13, cy - 6, paint);
            canvas.drawLine(cx - 10, cy - 3, cx - 14, cy - 7, paint);
            canvas.drawLine(cx - 2, cy - 6, cx - 2, cy - 10, paint);
        }

        private void drawSausage(Canvas canvas, float cx, float cy) {
            canvas.drawOval(cx - 13, cy - 5, cx + 13, cy + 5, paint);
            canvas.drawLine(cx - 15, cy, cx - 19, cy - 3, paint);
            canvas.drawLine(cx + 15, cy, cx + 19, cy + 3, paint);
            canvas.drawLine(cx - 4, cy - 5, cx + 3, cy + 5, paint);
        }

        private void drawChicken(Canvas canvas, float cx, float cy) {
            canvas.drawOval(cx - 11, cy - 1, cx + 7, cy + 10, paint);
            canvas.drawCircle(cx + 6, cy - 6, 4, paint);
            canvas.drawLine(cx + 10, cy - 6, cx + 14, cy - 8, paint);
            canvas.drawLine(cx - 3, cy + 10, cx - 7, cy + 14, paint);
            canvas.drawLine(cx + 2, cy + 10, cx + 6, cy + 14, paint);
        }

        private void drawPig(Canvas canvas, float cx, float cy) {
            canvas.drawOval(cx - 12, cy - 5, cx + 10, cy + 7, paint);
            canvas.drawCircle(cx + 11, cy - 3, 4, paint);
            canvas.drawLine(cx - 7, cy + 7, cx - 7, cy + 12, paint);
            canvas.drawLine(cx + 4, cy + 7, cx + 4, cy + 12, paint);
            canvas.drawLine(cx - 12, cy - 2, cx - 16, cy - 5, paint);
        }

        private void drawHeart(Canvas canvas, float cx, float cy) {
            android.graphics.Path path = new android.graphics.Path();
            path.moveTo(cx, cy + 10);
            path.cubicTo(cx - 15, cy, cx - 12, cy - 12, cx - 3, cy - 8);
            path.cubicTo(cx, cy - 15, cx + 15, cy - 10, cx + 10, cy + 1);
            path.cubicTo(cx + 8, cy + 5, cx + 4, cy + 8, cx, cy + 10);
            canvas.drawPath(path, paint);
        }
    }
}
