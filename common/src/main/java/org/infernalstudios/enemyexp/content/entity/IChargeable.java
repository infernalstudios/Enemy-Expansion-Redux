package org.infernalstudios.enemyexp.content.entity;

public interface IChargeable {
    int getChargeTime();

    void setChargeTime(int time);

    float getChargeDirX();

    void setChargeDirX(float x);

    float getChargeDirZ();

    void setChargeDirZ(float z);
}
