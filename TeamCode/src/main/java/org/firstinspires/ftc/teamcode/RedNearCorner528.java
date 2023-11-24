package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.eventloop.opmode.Autonomous;

@Autonomous(name = "RedNearCorner528", group = "Autonomous")
public class RedNearCorner528 extends AutoMaster {
    public RedNearCorner528() {
        super(BotPosition.RED_NEAR);
    }

    protected double boardDirectionFactor()
    {
        return(1);
    }

    protected double riggingDirectionFactor()
    {
        return(-1);
    }

    protected int aprilTagNumber(PropPosition propPosition) {
        int tagNumber =  4;

        if (propPosition == PropPosition.MIDDLE) {
            tagNumber = 5;
        }

        if (propPosition == PropPosition.FAR) {
            tagNumber = 6;
        }
        return(tagNumber);
    }
    protected double parkStrafeVector(int targetAprilTagNumber)
    {
        double slotsFromParkingPosition = 7 - targetAprilTagNumber + 0.5;
        return(slotsFromParkingPosition * Constants.distanceBetweenAprilTags + Constants.sensorToDrivetrainMiddle * 2);
    }
}

