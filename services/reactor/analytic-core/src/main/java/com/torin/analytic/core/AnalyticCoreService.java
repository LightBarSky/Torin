package com.torin.analytic.core;


public class AnalyticCoreService {

    private static final double ZERO = 0.0;

    @SuppressWarnings("unused")
    private double safeDivide(Number numerator, Number denominator) {
        if (numerator == null || denominator == null) {
            return ZERO;
        }

        double den = denominator.doubleValue();
        if (den == 0.0) {
            return ZERO;
        }

        return numerator.doubleValue() / den;
    }

    private double safeDivide(double numerator, double denominator) {
        if (denominator == 0.0) {
            return ZERO;
        }
        return numerator / denominator;
    }

    public double au(long uniqActions, long days) {
        return safeDivide(uniqActions, days);
    }

    public double stickinessRatio(double dau, double mau) {
        return safeDivide(dau, mau);
    }

    public double usageRegularity(double wau, double mau) {
        return safeDivide(wau, mau);
    }

    public double postsPerDay(long posts, long days) {
        return safeDivide(posts, days);
    }

    public double reactionsPerPost(long reactions, long posts) {
        return safeDivide(reactions, posts);
    }

    public double commentsPerPost(long comments, long posts) {
        return safeDivide(comments, posts);
    }

    public double engagementRate(double au, double reactionsPerPost, double commentsPerPost) {
        if (au == 0.0) {
            return ZERO;
        }
        return (reactionsPerPost + commentsPerPost) / au;
    }

    public double growthRate(long membersEnd, long membersStart) {
        if (membersStart == 0) {
            return ZERO;
        }
        return (double) (membersEnd - membersStart) / membersStart;
    }

    public Double reactionRate(long reactions, long views) {
        return safeDivide(reactions, views);
    }

    public double viewRate(long views, long members, long posts) {
        if (members == 0 || posts == 0) {
            return ZERO;
        }

        double denominator = (double) members * posts;
        return safeDivide(views, denominator);
    }

    public double commentRate(long comments, long views) {
        return safeDivide(comments, views);
    }

    public double erView(long reactions, long comments, long views) {
        return safeDivide(reactions + comments, views);
    }

    public double writerToMembers(long writers, long members) {
        return safeDivide(writers, members);
    }

    public double writerShare(long writers, long active) {
        return safeDivide(writers, active);
    }

    public double top10Share(long top, long all) {
        return safeDivide(top, all);
    }

    public double timeBurstIndex(long maxMh, long avgMh) {
        if (avgMh <= 0) {
            return ZERO;
        }
        return (double) maxMh / (avgMh + 1);
    }
}
