package org.firstinspires.ftc.teamcode.auto;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.util.Range;

import org.firstinspires.ftc.teamcode.common.Bot;
import org.firstinspires.ftc.teamcode.common.Constants;

import com.qualcomm.robotcore.util.ElapsedTime;

import java.util.List;

import org.firstinspires.ftc.teamcode.common.*;
import org.firstinspires.ftc.vision.apriltag.AprilTagDetection;
import org.firstinspires.ftc.vision.apriltag.AprilTagProcessor;

public abstract class AutoMaster extends LinearOpMode {
    protected Alliance alliance;
    protected StartPosition startPosition;
    protected ParkPosition parkPosition;
    protected Bot bot;
    protected VisionSensor visionSensor;

    protected AutoMaster(Alliance alliance, StartPosition startPosition, ParkPosition parkPosition) {
        this.alliance = alliance;
        this.startPosition = startPosition;
        this.parkPosition = parkPosition;
    }

    @Override
    public void runOpMode() {
        int riggingDirection;
        int boardDirection;
        int parkDirection;
        int targetAprilTagNumber;
        PropPipeline propProcessor = null;
        AprilTagProcessor aprilTagProcessor;
        PropDirection propDirection = null;
        ElapsedTime runTimer = new ElapsedTime();

        bot = new Bot(this, getMaxSpeed());

        visionSensor = new VisionSensor(this, alliance);

        riggingDirection = determineRiggingDirection();

        boardDirection = determineBoardDirection(riggingDirection);

        parkDirection = determineParkDirection(parkPosition, boardDirection);

        visionSensor.goToPropDetectionMode();
        bot.grabberClose();
        bot.wristUp();
        bot.pusherUp();
        sleep(500);

        while (!isStarted() && !isStopRequested()) {
            propDirection = visionSensor.getPropDirection();

            telemetry.addData("Prop Position: ", propDirection);
            telemetry.update();

            sleep(50);
        }
        if (!isStopRequested()) {
            runTimer.reset();
            visionSensor.goToNoSensingMode();
            setToLowCruisingPosition();

            // Move forward to escape position
            bot.moveStraightForDistance(Constants.pdDistanceToEscapePosition);

            // Place pixel on correct spike mark and return to escape position
            // Use propDirection determined using webcam during initlacePropPixel(propDirection, riggingDirection);

            roughTravelToBoard(boardDirection, riggingDirection);

            visionSensor.goToAprilTagDetectionMode();

            targetAprilTagNumber = getTargetAprilTagNumber(alliance, propDirection);

            roughAlignToAprilTag(boardDirection, targetAprilTagNumber, startPosition);
//        if (runTimer.time() <= orientMaxTime()) {
            // Correct strafe to directly face the target April Tag
            autoOrientToAprilTag(visionSensor, targetAprilTagNumber, boardDirection);
//        }

//        if (runTimer.time() <= placeMaxTime()) {            // Correct strafe to directly face the target April Tag
            placePixelOnBoard();
//        }

//        if (runTimer.time() <= parkMaxTime()) {
            park(boardDirection, targetAprilTagNumber, parkDirection);
//        }
//        telemetry.addData("finish park Time: ",runTimer.time());
//        telemetry.update();
//        sleep(1000);
            // Lower lift, lower wrist, open grabber

            setToTeleopStartingPosition();
        }
    }


    protected double getMaxSpeed() {
        return (Constants.maxAutoSpeed);
    }

    protected int determineRiggingDirection() {
        if (((startPosition == StartPosition.FAR) && (alliance == Alliance.BLUE)) ||
                ((startPosition == StartPosition.NEAR) && (alliance == Alliance.RED))) {
            return (-1);
        } else {
            return (1);
        }
    }

    protected double orientMaxTime() {
        return (15);
    }

    protected double placeMaxTime() {
        return (20);
    }

    protected double parkMaxTime() {
        return (24);
    }

    protected int determineBoardDirection(int riggingDirection) {
        if (startPosition == StartPosition.FAR) {
            return (riggingDirection);
        } else {
            return (-riggingDirection);
        }
    }

    protected int determineParkDirection(ParkPosition parkPosition, int boardDirection) {
        if (parkPosition == ParkPosition.CORNER) {
            return (boardDirection);
        } else if (parkPosition == ParkPosition.CENTER) {
            return (-boardDirection);
        } else {
            return (0);
        }
    }

    protected void setToHighCruisingPosition() {
        bot.liftStopAtPosition(Constants.liftAutoHighCruisingPosition);
        sleep(500);
        bot.wristUp();
    }

    protected void setToLowCruisingPosition() {
        bot.grabberClose();
        bot.wristUp();
        bot.liftStopAtPosition(Constants.liftAutoLowCruisingPosition);
    }

    protected void placePropPixel(PropDirection propDirection, int riggingDirection) {
        double setupDistance = 0;
        double placementDistance = Constants.pdCenterPlacementDistance;
        double heading = Constants.pdCenterHeading;
        double correctionDistance = 0;

        if (propDirection == PropDirection.LEFT) {
            placementDistance = Constants.pdLeftPlacementDistance;
            heading = Constants.pdLeftHeading;
            correctionDistance = 4;
            setupDistance = 15;
        } else if (propDirection == PropDirection.RIGHT) {
            placementDistance = Constants.pdRightPlacementDistance;
            heading = Constants.pdRightHeading;
            setupDistance = 15;
        }

//        telemetry.addData("propDirection: ", propDirection);
//        telemetry.addData("Heading: ", heading);
//        telemetry.addData("Distance: ", distance);
//        telemetry.update();
//        sleep(5000);

        bot.moveStraightForDistance(setupDistance);
        bot.turnToHeading(heading);
        bot.moveStraightForDistance(placementDistance);
        bot.moveStraightForDistance(-placementDistance - correctionDistance);
        bot.turnToHeading(0);
        bot.strafeForDistance(-correctionDistance);
        bot.moveStraightForDistance(-setupDistance);
        bot.strafeForDistance(-(riggingDirection * Constants.pdEscapeStrafeDistance));
    }

    protected void roughTravelToBoard(int boardPosition, int riggingDirection) {
        bot.turnToHeading(riggingDirection * 90);
    }

    public int getTargetAprilTagNumber(Alliance alliance, PropDirection propDirection) {
        int aprilTagNumber = 5;

        if (alliance == Alliance.RED) {
            aprilTagNumber = 4;
            if (propDirection == PropDirection.CENTER) {
                aprilTagNumber = 5;
            } else if (propDirection == PropDirection.RIGHT) {
                aprilTagNumber = 6;
            }
        } else {
            aprilTagNumber = 1;
            if (propDirection == PropDirection.CENTER) {
                aprilTagNumber = 2;
            } else if (propDirection == PropDirection.RIGHT) {
                aprilTagNumber = 3;
            }
        }
        return (aprilTagNumber);
    }

    protected void roughAlignToAprilTag(int boardDirection,
                                        int targetAprilTagNumber, StartPosition startPosition) {
        double strafeVector = 0;
        double chassisWidth = 2 * Constants.sensorToDrivetrainMiddle;
        if (boardDirection == -1) {
            if (startPosition == StartPosition.FAR) {
                strafeVector = (targetAprilTagNumber - 4 - 1.2) * Constants.distanceBetweenAprilTags;
            } else {
                strafeVector = chassisWidth + (targetAprilTagNumber - .6) * Constants.distanceBetweenAprilTags;
            }
        } else {
            if (startPosition == StartPosition.FAR) {
                strafeVector = chassisWidth + (targetAprilTagNumber - 4 + 2) * Constants.distanceBetweenAprilTags;
            } else {
                strafeVector = -2 - (6 - targetAprilTagNumber) * Constants.distanceBetweenAprilTags;
            }
        }
        bot.strafeForDistance(strafeVector);
    }

    protected void autoOrientToAprilTag(VisionSensor visionSensor, int targetTagNumber, int boardDirection) {

        AprilTagDetection targetTag;
        boolean targetFound = false;
        double strafeError;
        double yawError;
        double strafePower = 1;
        double yawPower = 1;
        double minStrafePower = .01;
        double minYawPower = .01;
        double currentHeading = 0;
        double startHeading = bot.getHeading();
        double maxScanHeading = startHeading + 15;
        double minScanHeading = startHeading - 15;
        ElapsedTime scanTimer = new ElapsedTime();
        double scanDuration = 4;
        double yawSearchPower = .15;
        double scanDirection = 1;

        scanTimer.reset();

        while (((Math.abs(strafePower) > minStrafePower) || (Math.abs(yawPower) > minYawPower))
                && (scanTimer.time() < scanDuration)) {
            targetFound = false;
            targetTag = null;

//            telemetry.addData("strafePower: ",strafePower);
//            telemetry.addData("yawPower: ",yawPower);
//            telemetry.update();
//            sleep(500);

            // Search through detected tags to find the target tag
            List<AprilTagDetection> currentDetections = visionSensor.getAprilTagDetections();
            for (AprilTagDetection detection : currentDetections) {
//                telemetry.addData("Iter Detections: ", targetFound);
//                telemetry.update();
//                sleep(100);

                // Look to see if we have size info on this tag
                if (detection.metadata != null) {
                    //  Check to see if we want to track towards this tag
                    if (detection.id == targetTagNumber) {
                        // Yes, we want to use this tag.
                        targetFound = true;
                        targetTag = detection;
                        break;  // don't look any further.
                    } else {
                        // This tag is in the library, but we do not want to track it right now.
                        telemetry.addData("Skipping", "Tag ID %d is not desired", detection.id);
                    }
                } else {
                    // This tag is NOT in the library, so we don't have enough information to track to it.
                    telemetry.addData("Unknown", "Tag ID %d is not in TagLibrary", detection.id);
                }
            }

            // Tell the driver what we see, and what to do.
            if (targetFound) {
                telemetry.addData("Found", "ID %d (%s)", targetTag.id, targetTag.metadata.name);
                telemetry.addData("Range", "%5.1f inches", targetTag.ftcPose.range);
                telemetry.addData("Bearing", "%3.0f degrees", targetTag.ftcPose.bearing);
                telemetry.addData("Yaw", "%3.0f degrees", targetTag.ftcPose.yaw);

                // Determine heading, range and Yaw (tag image rotation) error so we can use them to control the robot automatically.
                strafeError = targetTag.ftcPose.bearing;
                yawError = targetTag.ftcPose.yaw;

                // Use the speed and turn "gains" to calculate how we want the robot to move.
                strafePower = Range.clip(-yawError * Constants.atStrafeGain, -Constants.atMaxStrafe, Constants.atMaxStrafe);
                yawPower = Range.clip(strafeError * Constants.atYawGain, -Constants.atMaxYaw, Constants.atMaxYaw);

                telemetry.addData("Auto", "Strafe %5.2f", strafePower);
                telemetry.addData("Auto", "Yaw %5.2f", yawPower);
            } else {
                strafePower = 0;
                currentHeading = bot.getHeading();
                if (currentHeading <= minScanHeading) {
                    scanDirection = 1;
                }
                if (currentHeading >= maxScanHeading) {
                    scanDirection = -1;
                }
                yawPower = scanDirection * yawSearchPower;
            }
            telemetry.update();
            // Apply desired axes motions to the drivetrain.
            bot.moveDirection(0, strafePower, yawPower);
            sleep(10);
        }
        if (!targetFound) {
            bot.turnToHeading(boardDirection * -90);
        }
    }

    protected void placePixelOnBoard() {
        bot.moveStraightForDistance(Constants.boardApproachDistance);
        bot.strafeForDistance(-Constants.sensorToDrivetrainMiddle);
        bot.liftStopAtPosition(Constants.liftAutoBoardProbePosition);
        bot.wristDown();
        bot.creepUntilContact();
        bot.creepStraightForDistance(-Constants.boardOffsetDistance);
        bot.wristUp();
//        bot.wristToPosition(Constants.wristUpPosition);
        bot.liftStopAtPosition(Constants.liftAutoBoardPlacementPosition);
        sleep(250);
        bot.creepStraightForDistance(Constants.boardOffsetDistance + 4.5);
        bot.grabberOpen();
        sleep(250);
        bot.moveStraightForDistance(-Constants.boardEscapeDistance);
    }

    protected void park(double boardDirection, int targetAprilTagNumber, double parkDirection) {
        double strafeVector = 0;
        int adjustedTagNumber = targetAprilTagNumber;
        if (targetAprilTagNumber > 3) {
            adjustedTagNumber = adjustedTagNumber - 3;
        }
        strafeVector = 2.5 * Constants.sensorToDrivetrainMiddle;

        if (parkDirection > 0) {
            strafeVector = strafeVector + (4.5 - adjustedTagNumber) * Constants.distanceBetweenAprilTags;
        } else {
            strafeVector = -strafeVector - 3 - adjustedTagNumber * Constants.distanceBetweenAprilTags;
        }

        bot.turnToHeading(boardDirection * 90);
        bot.strafeForDistance(-strafeVector);
        bot.moveStraightForDistance(-14);
    }

    protected void setToTeleopStartingPosition() {
        bot.grabberClose();
        bot.liftStopAtPosition(Constants.liftAutoHighCruisingPosition);
        bot.wristMiddle();
        sleep(250);
        bot.liftStopAtPosition(0);
        sleep(2500);
    }
}