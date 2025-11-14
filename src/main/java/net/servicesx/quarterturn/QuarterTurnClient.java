package net.servicesx.quarterturn;

import net.fabricmc.api.ClientModInitializer;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.text.LiteralText;
import org.lwjgl.input.Keyboard;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

public class QuarterTurnClient implements ClientModInitializer {

    private static final int KEY_TOGGLE = Keyboard.KEY_R;
    private static final int KEY_RECORD = Keyboard.KEY_P;
    private static final int KEY_RESET  = Keyboard.KEY_O;

    private static final int MAX_CORNERS = 4;
    private static final float STEP_DEGREES = 7.3F;
    private static final int CORNER_HOLD_TICKS = 2;

    private static boolean togglePrev = false;
    private static boolean recordPrev = false;
    private static boolean resetPrev  = false;

    private static final float[] cornerYaw = new float[MAX_CORNERS];
    private static final float[] cornerPitch = new float[MAX_CORNERS];
    private static int cornerCount = 0;
    private static int currentCorner = 0;

    private static boolean enabled = false;
    private static int attackCooldown = 0;

    private static Method keyOnTickMethod;

    @Override
    public void onInitializeClient() {
        Thread loopThread = new Thread(QuarterTurnClient::loop, "QuarterTurn-Tick");
        loopThread.setDaemon(true);
        loopThread.start();
    }

    private static void loop() {
        while (true) {
            try {
                Thread.sleep(50L);
            } catch (InterruptedException e) {
                return;
            }
            tick();
        }
    }

    private static void tick() {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc == null) return;
        if (mc.world == null) return;
        if (mc.player == null) return;

        handleKeys(mc);

        if (enabled && cornerCount > 0) {
            rotateTowardsNextCorner(mc);
        }
    }

    private static void handleKeys(MinecraftClient mc) {
        boolean toggleNow = Keyboard.isKeyDown(KEY_TOGGLE);
        if (toggleNow && !togglePrev) {
            enabled = !enabled;
            attackCooldown = 0;
            if (enabled) {
                currentCorner = 0;
                if (cornerCount == 0) {
                    sendChat(mc, "QuarterTurn enabled, but no corners set. Look somewhere and press P to set 1/" + MAX_CORNERS + ".");
                } else {
                    sendChat(mc, "QuarterTurn enabled. Rotating through " + cornerCount + "/" + MAX_CORNERS + " corners.");
                }
            } else {
                sendChat(mc, "QuarterTurn disabled.");
            }
        }
        togglePrev = toggleNow;

        boolean recordNow = Keyboard.isKeyDown(KEY_RECORD);
        if (recordNow && !recordPrev) {
            recordCorner(mc);
        }
        recordPrev = recordNow;

        boolean resetNow = Keyboard.isKeyDown(KEY_RESET);
        if (resetNow && !resetPrev) {
            resetMacro(mc);
        }
        resetPrev = resetNow;
    }

    private static void recordCorner(MinecraftClient mc) {
        if (cornerCount < MAX_CORNERS) {
            cornerYaw[cornerCount] = mc.player.yaw;
            cornerPitch[cornerCount] = mc.player.pitch;
            cornerCount++;
            sendChat(mc, "QuarterTurn: corner " + cornerCount + "/" + MAX_CORNERS + " saved.");
        } else {
            cornerYaw[currentCorner] = mc.player.yaw;
            cornerPitch[currentCorner] = mc.player.pitch;
            int displayIndex = currentCorner + 1;
            currentCorner = (currentCorner + 1) % MAX_CORNERS;
            sendChat(mc, "QuarterTurn: corner " + displayIndex + "/" + MAX_CORNERS + " updated.");
        }
    }

    private static void resetMacro(MinecraftClient mc) {
        enabled = false;
        cornerCount = 0;
        currentCorner = 0;
        attackCooldown = 0;
        for (int i = 0; i < MAX_CORNERS; i++) {
            cornerYaw[i] = 0.0F;
            cornerPitch[i] = 0.0F;
        }
        sendChat(mc, "QuarterTurn reset. All corners cleared.");
    }

    private static void rotateTowardsNextCorner(MinecraftClient mc) {
        float targetYaw = cornerYaw[currentCorner];
        float targetPitch = cornerPitch[currentCorner];

        float yaw = mc.player.yaw;
        float pitch = mc.player.pitch;

        float dyaw = wrapDegrees(targetYaw - yaw);
        float dpitch = targetPitch - pitch;

        boolean yawDone = Math.abs(dyaw) <= STEP_DEGREES;
        boolean pitchDone = Math.abs(dpitch) <= STEP_DEGREES;

        if (yawDone && pitchDone) {
            mc.player.yaw = targetYaw;
            mc.player.pitch = targetPitch;

            if (attackCooldown < CORNER_HOLD_TICKS) {
                attackCooldown++;
                return;
            }

            attackCooldown = 0;
            doAttack(mc);
            currentCorner = (currentCorner + 1) % cornerCount;
        } else {
            attackCooldown = 0;
            if (!yawDone) {
                float stepYaw = clamp(dyaw, -STEP_DEGREES, STEP_DEGREES);
                mc.player.yaw = yaw + stepYaw;
            }
            if (!pitchDone) {
                float stepPitch = clamp(dpitch, -STEP_DEGREES, STEP_DEGREES);
                mc.player.pitch = pitch + stepPitch;
            }
        }
    }

    private static void doAttack(MinecraftClient mc) {
        if (mc.options == null) return;
        if (keyOnTickMethod == null) {
            try {
                for (Method m : KeyBinding.class.getDeclaredMethods()) {
                    if (Modifier.isStatic(m.getModifiers())
                            && m.getParameterTypes().length == 1
                            && m.getParameterTypes()[0] == int.class
                            && m.getReturnType() == void.class) {
                        m.setAccessible(true);
                        keyOnTickMethod = m;
                        break;
                    }
                }
            } catch (Throwable ignored) {
            }
        }
        if (keyOnTickMethod != null) {
            try {
                int key = mc.options.attackKey.getCode();
                keyOnTickMethod.invoke(null, key);
            } catch (Throwable ignored) {
            }
        }
    }

    private static void sendChat(MinecraftClient mc, String message) {
        if (mc.player != null) {
            mc.player.sendMessage(new LiteralText(message));
        }
    }

    private static float clamp(float v, float min, float max) {
        if (v < min) return min;
        if (v > max) return max;
        return v;
    }

    private static float wrapDegrees(float value) {
        value %= 360.0F;
        if (value >= 180.0F) {
            value -= 360.0F;
        }
        if (value < -180.0F) {
            value += 360.0F;
        }
        return value;
    }
}