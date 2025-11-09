package theater;

import java.text.NumberFormat;
import java.util.Locale;
import java.util.Map;

/**
 * This class generates a statement for a given invoice of performances.
 */
public class StatementPrinter {
    private Invoice invoice;
    private Map<String, Play> plays;

    /**
     * Creates a new statement printer.
     *
     * @param invoice the invoice containing the performances
     * @param plays   the map from play ID to play information
     */
    public StatementPrinter(Invoice invoice, Map<String, Play> plays) {
        this.invoice = invoice;
        this.plays = plays;
    }

    /**
     * Returns the invoice for this printer.
     *
     * @return the invoice
     */
    public Invoice getInvoice() {
        return invoice;
    }

    /**
     * Returns the map of plays for this printer.
     *
     * @return the plays map
     */
    public Map<String, Play> getPlays() {
        return plays;
    }

    /**
     * Returns a formatted statement of the invoice associated with this printer.
     *
     * @return the formatted statement
     * @throws RuntimeException if one of the play types is not known
     */
    public String statement() {
        final int volumeCredits = getTotalVolumeCredits();
        final int totalAmount = getTotalAmount();
        final StringBuilder result = new StringBuilder(
                "Statement for " + invoice.getCustomer() + System.lineSeparator());

        for (Performance p : invoice.getPerformances()) {
            result.append(String.format("  %s: %s (%s seats)%n",
                    getPlay(p).getName(),
                    usd(getAmount(p)),
                    p.getAudience()));
        }

        result.append(String.format("Amount owed is %s%n", usd(totalAmount)));
        result.append(String.format("You earned %s credits%n", volumeCredits));
        return result.toString();
    }

    /**
     * Calculates the total amount owed for all performances in the invoice.
     *
     * @return the total amount in cents
     */
    public int getTotalAmount() {
        int totalAmount = 0;
        for (Performance p : invoice.getPerformances()) {
            totalAmount += getAmount(p);
        }
        return totalAmount;
    }

    /**
     * Calculates the total volume credits earned for all performances
     * in the invoice.
     *
     * @return the total volume credits
     */
    public int getTotalVolumeCredits() {
        int volumeCredits = 0;
        for (Performance p : invoice.getPerformances()) {
            volumeCredits += getVolumeCredits(p);
        }
        return volumeCredits;
    }

    /**
     * Calculates the volume credits earned for a single performance.
     *
     * @param performance the performance to evaluate
     * @return the volume credits earned for the performance
     */
    public int getVolumeCredits(Performance performance) {
        int result = 0;

        result += Math.max(
                performance.getAudience() - Constants.BASE_VOLUME_CREDIT_THRESHOLD, 0);

        if ("comedy".equals(getPlay(performance).getType())) {
            result += performance.getAudience() / Constants.COMEDY_EXTRA_VOLUME_FACTOR;
        }

        return result;
    }

    /**
     * Formats an amount in cents as a US currency string.
     *
     * @param amount the amount in cents
     * @return the formatted currency string
     */
    public static String usd(int amount) {
        return NumberFormat.getCurrencyInstance(Locale.US).format(
                amount / (double) Constants.PERCENT_FACTOR);
    }

    /**
     * Returns the play associated with the given performance.
     *
     * @param performance the performance whose play is requested
     * @return the play corresponding to the performance
     */
    public Play getPlay(Performance performance) {
        return plays.get(performance.getPlayID());
    }

    /**
     * Calculates the charge amount, in cents, for a single performance.
     *
     * @param performance the performance to price
     * @return the charge amount in cents
     * @throws RuntimeException if the play type is unknown
     */
    public int getAmount(Performance performance) {
        int result = 0;
        final Play play = getPlay(performance);

        switch (play.getType()) {
            case "tragedy":
                result = Constants.TRAGEDY_BASE_AMOUNT;
                if (performance.getAudience() > Constants.TRAGEDY_AUDIENCE_THRESHOLD) {
                    result += Constants.TRAGEDY_EXTRA_PER_AUDIENCE
                            * (performance.getAudience() - Constants.TRAGEDY_AUDIENCE_THRESHOLD);
                }
                break;
            case "comedy":
                result = Constants.COMEDY_BASE_AMOUNT;
                if (performance.getAudience() > Constants.COMEDY_AUDIENCE_THRESHOLD) {
                    result += Constants.COMEDY_OVER_BASE_CAPACITY_AMOUNT
                            + (Constants.COMEDY_OVER_BASE_CAPACITY_PER_PERSON
                            * (performance.getAudience() - Constants.COMEDY_AUDIENCE_THRESHOLD));
                }
                result += Constants.COMEDY_AMOUNT_PER_AUDIENCE * performance.getAudience();
                break;
            default:
                throw new RuntimeException(String.format("unknown type: %s", play.getType()));
        }
        return result;
    }
}
