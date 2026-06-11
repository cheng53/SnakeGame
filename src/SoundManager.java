import javax.sound.sampled.*;
import java.io.File;
import javax.swing.Timer; // ✨ 補上這一行！

public class SoundManager {
    private Clip bgmClip; // 專門用來控制背景音樂的物件 (因為背景音樂需要能被暫停或停止)

    // 🎵 播放背景音樂 (會自動無限循環)
    public void playBGM(String filePath) {
        try {
            // 如果已經有音樂在播，先停止它
            if (bgmClip != null && bgmClip.isRunning()) {
                bgmClip.stop();
            }
            File musicPath = new File(filePath);
            if (musicPath.exists()) {
                AudioInputStream audioInput = AudioSystem.getAudioInputStream(musicPath);
                bgmClip = AudioSystem.getClip();
                bgmClip.open(audioInput);
                bgmClip.loop(Clip.LOOP_CONTINUOUSLY); // 設定為無限循環
                bgmClip.start();
            } else {
                System.out.println("找不到背景音樂檔案: " + filePath);
            }
        } catch (Exception e) {
            System.out.println("播放背景音樂失敗: " + e.getMessage());
        }
    }

    // 🛑 停止背景音樂
    public void stopBGM() {
        if (bgmClip != null && bgmClip.isRunning()) {
            bgmClip.stop();
        }
    }

    // 🔊 播放單次音效 (吃蘋果、死亡、過關等)
    public void playSound(String filePath) {
        try {
            File soundPath = new File(filePath);
            if (soundPath.exists()) {
                AudioInputStream audioInput = AudioSystem.getAudioInputStream(soundPath);
                Clip clip = AudioSystem.getClip();
                clip.open(audioInput);
                clip.start();
            } else {
                System.out.println("找不到音效檔案: " + filePath);
            }
        } catch (Exception e) {
            System.out.println("播放音效失敗: " + e.getMessage());
        }
    }
    // ✨ 新增：可以「跳過前面特定秒數」並且「限制播放時間」的音效播放
    public void playSoundWithOffsetAndTimeLimit(String filePath, int startMs, int durationMs) {
        try {
            File soundPath = new File(filePath);
            if (soundPath.exists()) {
                AudioInputStream audioInput = AudioSystem.getAudioInputStream(soundPath);
                Clip clip = AudioSystem.getClip();
                clip.open(audioInput);

                // 💡 關鍵：跳過前面指定的毫秒數 (1 毫秒 = 1000 微秒)
                clip.setMicrosecondPosition(startMs * 1000L);

                clip.start();

                // 建立一個計時器，經過 durationMs 時間後強制停止並釋放資源
                Timer stopTimer = new Timer(durationMs, e -> {
                    if (clip.isRunning()) {
                        clip.stop();
                    }
                    clip.close();
                    ((Timer)e.getSource()).stop();
                });
                stopTimer.setRepeats(false);
                stopTimer.start();
            } else {
                System.out.println("找不到音效檔案: " + filePath);
            }
        } catch (Exception e) {
            System.out.println("播放受限音效失敗: " + e.getMessage());
        }
    }}