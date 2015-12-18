package ru.ivied.wastedtime;

import android.text.format.DateUtils;

public class TimeToMoneyConverter {

    public  static class Money{
        int dollars;
        int cents;

        public Money(int dollars, int cents) {
            this.dollars = dollars;
            this.cents = cents;
        }

        public int getDollars() {
            return dollars;
        }

        public int getCents() {
            return cents;
        }
    }

    public static Money convertMillis(long millis, double hourlyRate){
        double hours = (double) millis / DateUtils.HOUR_IN_MILLIS;

        int money = (int) (hours * hourlyRate * 100);
        int dollars = money /100 ;
        int cents = money % 100;


        return new Money(dollars, cents);
    }

    public static String moneyToString(Money money){
        return String.format("%d$ %02d¢", money.getDollars(), money.getCents());
    }
}
