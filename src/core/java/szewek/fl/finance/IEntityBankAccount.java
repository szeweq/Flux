package szewek.fl.finance;

public interface IEntityBankAccount {
    long getCurrentAmount();

    class Dummy implements IEntityBankAccount {
        @Override
        public long getCurrentAmount() {
            return 0;
        }
    }
}
