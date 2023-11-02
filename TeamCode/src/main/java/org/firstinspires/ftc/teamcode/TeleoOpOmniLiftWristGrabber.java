package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

@TeleOp(name = "TeleoOpOmniLiftWristGrabber", group = "Linear OpMode")
public class TeleoOpOmniLiftWristGrabber extends LinearOpMode {
    private TeleOpBot bot = null;

    @Override
    public void runOpMode() {
        double driveAxial = 0.0;
        double driveStrafe = 0.0;
        double driveYaw = 0.0;
        double leftTrigger = 0.0;
        double rightTrigger = 0.0;

        bot = new TeleOpBot(this);
        waitForStart();

        while (opModeIsActive()) {
            if (gamepad1.dpad_up) {
                bot.creepDirection(1.0, 0.0, 0.0);
            } else if (gamepad1.dpad_down) {
                bot.creepDirection(-1.0, 0.0, 0.0);
            } else if (gamepad1.dpad_left) {
                bot.creepDirection(0.0, 1.0, 0.0);
            } else if (gamepad1.dpad_right) {
                bot.creepDirection(0.0, -1.0, 0.0);
            } else {
                driveAxial = gamepad1.left_stick_y;
                driveStrafe = gamepad1.left_stick_x;
                driveYaw = gamepad1.right_stick_x;
                if ((Math.abs(driveAxial) < 0.3) && (Math.abs(driveStrafe) < 0.3) && (Math.abs(driveYaw) < 0.3)) {
                    bot.stopDrive();
                } else
                    bot.moveDirection(-driveAxial, -driveStrafe, -driveYaw);
            }

            leftTrigger = gamepad1.left_trigger;
            rightTrigger = gamepad1.right_trigger;
            if (leftTrigger > 0.3) {
                bot.liftDown(leftTrigger);
            } else if (rightTrigger > 0.3) {
                bot.liftUp(rightTrigger);
            } else {
                bot.liftStop();
            }

            if (gamepad1.x) {
                bot.grabberClose();
            } else if (gamepad1.a) {
                bot.grabberOpen();
            }

            if (gamepad1.b) {
                bot.wristUp();
            } else if (gamepad1.y) {
                bot.wristDown();
            }

        }
    }
}
