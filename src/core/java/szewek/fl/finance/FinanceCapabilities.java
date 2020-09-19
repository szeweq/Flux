package szewek.fl.finance;

import net.minecraft.nbt.INBT;
import net.minecraft.util.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.CapabilityManager;

public final class FinanceCapabilities {

    @CapabilityInject(IEntityBankAccount.class)
    public static Capability<IEntityBankAccount> BANK_ACCOUNT_CAP = null;

    public static void register() {
        CapabilityManager.INSTANCE.register(IEntityBankAccount.class, new Capability.IStorage<IEntityBankAccount>() {
            @Override
            public INBT writeNBT(Capability<IEntityBankAccount> capability, IEntityBankAccount instance, Direction side) {
                return null;
            }

            @Override
            public void readNBT(Capability<IEntityBankAccount> capability, IEntityBankAccount instance, Direction side, INBT nbt) {

            }
        }, IEntityBankAccount.Dummy::new);
    }
}
