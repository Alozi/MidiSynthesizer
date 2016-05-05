package practice;

import javax.sound.midi.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.util.ArrayList;

class One {
    private JFrame frame;
    private ArrayList<JCheckBox> jCheckBoxArrayList;
    private String[] instrumentNames = {"Bass Drum", "Closed Hi-hat", "Open Hi-Hat", "Acoustic Snare", "Crash Cymbal", "Hand Clap", "High Tom", "Hi Bongo", "Maracas", "Whistle", "Low Conga", "Cowbell", "Vibraslap", "Low-mid Tom", "High Agogo", "Open Hi Conga"};
    private JPanel mainPanel;

    private Sequencer sequencer;
    private Sequence sequence;
    private Track track;

    int[] instrument = {35, 42, 46, 38, 49, 39, 50, 60, 70, 72, 64, 56, 58, 47, 67, 63};

    public static void main(String[] args) {
        One gui = new One();
        gui.setUpGUI();
    }

    public void setUpGUI() {

        frame = new JFrame("Cyber BeatBox.");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        BorderLayout layout = new BorderLayout();
        JPanel panel = new JPanel(layout);
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        jCheckBoxArrayList = new ArrayList<JCheckBox>();
        Box buttonBox = new Box(BoxLayout.Y_AXIS);

        JButton button0 = new JButton("Start");
        button0.addActionListener(new startButtonListener());
        buttonBox.add(button0);
        JButton button1 = new JButton("Stop");
        button1.addActionListener(new stopButtonListener());
        buttonBox.add(button1);
        JButton button2 = new JButton("Tempo up");
        button2.addActionListener(new tempoUpButtonListener());
        buttonBox.add(button2);
        JButton button3 = new JButton("Tempo down");
        button3.addActionListener(new tempoDownButtonListener());
        buttonBox.add(button3);

        JButton buttonSerialize = new JButton("serialize");
        buttonSerialize.addActionListener(new MySendListener());
        buttonBox.add(buttonSerialize);

        JButton buttonDeserialize = new JButton("restore");
        buttonDeserialize.addActionListener(new MyReadListener());
        buttonBox.add(buttonDeserialize);


        Box nameBox = new Box(BoxLayout.Y_AXIS);
        for (int i = 0; i < 16; i++) {
            nameBox.add(new Label(instrumentNames[i]));
        }

        panel.add(BorderLayout.EAST, buttonBox);
        panel.add(BorderLayout.WEST, nameBox);

        frame.getContentPane().add(panel);

        GridLayout gridLayout = new GridLayout(16, 16);
        mainPanel = new JPanel(gridLayout);
        panel.add(BorderLayout.CENTER, mainPanel);

        for (int i = 0; i < 256; i++) {
            JCheckBox check = new JCheckBox();
            check.setSelected(false);
            jCheckBoxArrayList.add(check);
            mainPanel.add(check);
        }
        setUpMidi();

        frame.setBounds(50, 50, 300, 300);
        frame.pack();
        frame.setVisible(true);
    }

    public void setUpMidi() {
        try {
            sequencer = MidiSystem.getSequencer();
            sequencer.open();
            sequence = new Sequence(Sequence.PPQ, 4);
            track = sequence.createTrack();
            sequencer.setTempoInBPM(120);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void buildTrackAndStart() {
        int[] trackList = null;
        sequence.deleteTrack(track);
        track = sequence.createTrack();

        for (int i = 0; i < 16; i++) {
            trackList = new int[16];
            int key = instrument[i];

            for (int j = 0; j < 16; j++) {
                JCheckBox checkBoxJ = (JCheckBox) jCheckBoxArrayList.get(j + (i * 16));
                if (checkBoxJ.isSelected()) {
                    trackList[j] = key;
                } else {
                    trackList[j] = 0;
                }
            }

            makeTracks(trackList);
            track.add(makeEvent(176, 1, 127, 0, 16));
        }

        track.add(makeEvent(192, 9, 1, 0, 15));
        try {
            sequencer.setSequence(sequence);
            sequencer.setLoopCount(sequencer.LOOP_CONTINUOUSLY);
            sequencer.start();
            sequencer.setTempoInBPM(120);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private class startButtonListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            buildTrackAndStart();
        }
    }

    private class stopButtonListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            sequencer.stop();
        }
    }

    private class tempoUpButtonListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            float tempoFactor = sequencer.getTempoFactor();
            sequencer.setTempoFactor((float) (tempoFactor * 1.03));
        }
    }

    private class tempoDownButtonListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            float tempoFactor = sequencer.getTempoFactor();
            sequencer.setTempoFactor((float) (tempoFactor * 0.97));
        }
    }

    public void makeTracks(int[] list) {
        for (int i = 0; i < 16; i++) {
            int key = list[i];

            if (key != 0) {
                track.add(makeEvent(144, 9, key, 100, i));
                track.add(makeEvent(128, 9, key, 100, i + 1));
            }
        }
    }

    public MidiEvent makeEvent(int comd, int chan, int one, int two, int tick) {
        MidiEvent event = null;
        try {
            ShortMessage shortMessage = new ShortMessage();
            shortMessage.setMessage(comd, chan, one, two);
            event = new MidiEvent(shortMessage, tick);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return event;
    }

    private class MySendListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            boolean[] checkboxState1 = new boolean[256];

            for (int i = 0; i < 256; i++) {
                JCheckBox checkBox = (JCheckBox) jCheckBoxArrayList.get(i);
                if (checkBox.isSelected()) {
                    checkboxState1[i] = true;

                }
            }
            try {
                FileOutputStream fileOutputStream = new FileOutputStream(new File("Checkbox.ser"));
                ObjectOutputStream objectOutputStream = new ObjectOutputStream(fileOutputStream);
                objectOutputStream.writeObject(checkboxState1);
            } catch (Exception ex) {
                ex.printStackTrace();
            }

        }
    }

    private class MyReadListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            boolean[] checkboxState = null;
            try {
                FileInputStream fileInputStream = new FileInputStream(new File("Checkbox.ser"));
                ObjectInputStream objectInputStream = new ObjectInputStream(fileInputStream);
                checkboxState = (boolean[]) objectInputStream.readObject();
            } catch (Exception e1) {
                e1.printStackTrace();
            }

            for (int i = 0; i < 256; i++) {
                JCheckBox checkBox = (JCheckBox) jCheckBoxArrayList.get(i);
                if (checkboxState[i]) {
                    checkBox.setSelected(true);
                } else {
                    checkBox.setSelected(false);
                }
            }

            sequencer.stop();
            buildTrackAndStart();
        }
    }
}
