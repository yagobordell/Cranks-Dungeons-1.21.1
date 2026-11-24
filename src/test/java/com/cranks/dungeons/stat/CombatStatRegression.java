package com.cranks.dungeons.stat;

public final class CombatStatRegression {
    public static void run() {
        require(CombatStatRules.armourReduction(600,200)>0.20,"Armour did not improve large-hit protection");
        require(CombatStatRules.armourReduction(600,20)>CombatStatRules.armourReduction(600,200),"Armour lost hit-size scaling");
        require(close(CombatStatRules.armourReduction(1e9,1),0.90),"Armour cap failed");
        require(close(CombatStatRules.evasionChance(1e9,1),0.75),"Evasion cap failed");
        require(close(CombatStatRules.blockedDamage(1000,100),85),"Shield chip damage limit failed");
        require(close(CombatStatRules.healthRegeneration(100,2,0.02,0.5),6),"Regeneration scaling incorrect");
        require(close(CombatStatRules.healthRegeneration(100,500,10,10),10),"Regeneration cap failed");
        require(close(CombatStatRules.manaRegeneration(100,3,0.04,0.5,0.5),15.75),"Flat mana regeneration missing");
        var pool=new RecoveryPool();
        for(int i=0;i<1000;i++)pool.offer(10,100);
        double received=0;for(int i=0;i<20;i++)received+=pool.drain(100);
        require(close(received,10),"Many hits bypassed the leech rate limit");
        received=0;for(int i=0;i<40;i++)received+=pool.drain(100);
        require(close(received,10)&&pool.empty(),"Leech pool unbounded or healing discarded");
        pool.offer(Double.NaN,100);pool.offer(-10,100);require(pool.empty(),"Invalid recovery accepted");
        var instant=new RecoveryPool();double burst=0;
        for(int i=0;i<100;i++){instant.offer(10,100);burst+=instant.drainInstant(100,1);}
        require(close(burst,10),"Instant leech bypassed shared budget");
        instant.offer(10,100);require(close(instant.drainInstant(100,11),5),"Instant budget refill incorrect");
        var resized=new RecoveryPool();resized.offer(100,1000);
        double resizedTotal=0;for(int i=0;i<100;i++)resizedTotal+=resized.drain(10);
        require(close(resizedTotal,2),"Changing resource maximum retained oversized recovery");
        require(close(CombatStatRules.criticalProtection(100,0.005,0.2),0.6),"Combined critical protection incorrect");
        require(close(CombatStatRules.shieldRechargeDelay(100),1),"Shield delay limit failed");
        require(close(CombatStatRules.shieldRechargeFraction(100),0.5),"Shield recharge limit failed");
        String custom="My custom node description";
        require(StatDescriptions.reviseNodeDescription(custom).equals(custom),"Custom tree text was overwritten");
        String updated=StatDescriptions.reviseNodeDescription("15% chance to Pierce an Enemy");
        require(StatDescriptions.reviseNodeDescription(updated).equals(updated),"Description migration is not idempotent");
    }
    private static boolean close(double a,double b){return Math.abs(a-b)<0.000001;}
    private static void require(boolean value,String message){if(!value)throw new AssertionError(message);}
}
