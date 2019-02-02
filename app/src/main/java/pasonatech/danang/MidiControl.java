package pasonatech.danang;

import android.content.Context;
import android.content.pm.PackageManager;
import android.media.midi.MidiDevice;
import android.media.midi.MidiDeviceInfo;
import android.media.midi.MidiInputPort;
import android.media.midi.MidiManager;
import android.view.Gravity;
import android.widget.Toast;

import java.io.IOException;

public class MidiControl {
    private Context context ;
    private static MidiManager midiManager ;
    private MidiDevice midiDevice ;
    private int InputPortNum ;
    private MidiInputPort inPort ;

    class MidiChannel{
        private byte channel ;
        private byte programNo;
        private MIDImode midiMode;

        public MidiChannel(){
            this.channel = 1;
        }

        public void setInstrument(byte InstrumentNo, MIDImode midiMode){
            if(0 == this.channel){
                MidiControl.this.toastMassage("Channel is not opened.");
                return;
            }
            this.allOff();
            byte[] buffer = new byte[16];
            int numBytes = 0;
            buffer[numBytes++] = (byte)(ChannelVoice.ProgramChange.getByte() | (this.getChannelNo() - 1)); // Program change
            buffer[numBytes++] = InstrumentNo; // Instrument
            buffer[numBytes++] = (byte)0;

            MidiControl.this.sendMidi(buffer,numBytes);
            this.programNo = (byte)InstrumentNo ;

            this.setMIDImode(midiMode, (byte)0);

        }
        public void setMIDImode( MIDImode midiMode, byte NumOfChannel ){
            this.allOff();
            if(0 == this.getChannelNo()){
                MidiControl.this.toastMassage("Channel is not initialized.");
                return;
            }
            byte[] buffer = new byte[16];
            int numBytes = 0;

            switch (midiMode) {
                default:
                    MidiControl.this.toastMassage("Illegal MIDI mode.");
                    return;

                case Mode1:
                    buffer[numBytes++] = (byte)(ChannelVoice.ControlChange.getByte() + this.getChannelNo() - 1); // Program change
                    buffer[numBytes++] = ChannelMode.OmniOn.getByte();
                    buffer[numBytes++] = (byte) 0;
                    buffer[numBytes++] = (byte) (ChannelVoice.ControlChange.getByte() + this.getChannelNo() - 1); // Program change
                    buffer[numBytes++] = ChannelMode.PolyMode.getByte();
                    buffer[numBytes++] = (byte) 0;
                    break;

                case Mode2:
                    buffer[numBytes++] = (byte) (ChannelVoice.ControlChange.getByte() + this.getChannelNo() - 1); // Program change
                    buffer[numBytes++] = ChannelMode.OmniOn.getByte();
                    buffer[numBytes++] = (byte) 0;
                    buffer[numBytes++] = (byte) (ChannelVoice.ControlChange.getByte() + this.getChannelNo() - 1); // Program change
                    buffer[numBytes++] = ChannelMode.MonoMode.getByte();
                    buffer[numBytes++] = (byte) 0;
                    break;

                case Mode3:
                    buffer[numBytes++] = (byte) (ChannelVoice.ControlChange.getByte() + this.getChannelNo() - 1); // Program change
                    buffer[numBytes++] = ChannelMode.OmniOff.getByte();
                    buffer[numBytes++] = (byte) 0;
                    buffer[numBytes++] = (byte) (ChannelVoice.ControlChange .getByte()+ this.getChannelNo() - 1); // Program change
                    buffer[numBytes++] = ChannelMode.PolyMode.getByte();
                    buffer[numBytes++] = (byte) 0;
                    break;

                case Mode4:
                    buffer[numBytes++] = (byte) (ChannelVoice.ControlChange.getByte() + this.getChannelNo() - 1); // Program change
                    buffer[numBytes++] = ChannelMode.OmniOff.getByte();
                    buffer[numBytes++] = (byte) 0;
                    buffer[numBytes++] = (byte) (ChannelVoice.ControlChange.getByte() + this.getChannelNo() - 1); // Program change
                    buffer[numBytes++] = ChannelMode.MonoMode.getByte();
                    buffer[numBytes++] = NumOfChannel;
                    break;
            }
            MidiControl.this.sendMidi(buffer,numBytes);
            this.midiMode = midiMode ;
        }
        public byte getChannelNo(){
            return this.channel ;
        }
        public byte getInstrumentNo(){
            return this.programNo ;
        }
        public MIDImode getMIDImode(){
            return this.midiMode ;
        }

        public void noteOn(byte pitch, byte velocity){
            if(0 == this.getChannelNo()){
                MidiControl.this.toastMassage("Channel is not initialized.");
                return;
            }
            // MIDIデータの作成
            byte[] buffer = new byte[16];
            int numBytes = 0;
            buffer[numBytes++] = (byte)(ChannelVoice.NoteOn.getByte() + this.getChannelNo() - 1); // note on
            buffer[numBytes++] = pitch; // pitch
            if(0>velocity){
                velocity = (byte)(velocity * -1);
            }
            buffer[numBytes++] = velocity; // velocity

            MidiControl.this.sendMidi(buffer,numBytes);
        }

        public void noteOff(byte pitch){
            if(0 == this.getChannelNo()){
                MidiControl.this.toastMassage("Channel is not initialized.");
                return;
            }
            // MIDIデータの作成
            byte[] buffer = new byte[16];
            int numBytes = 0;
            buffer[numBytes++] = (byte)(ChannelVoice.NoteOff.getByte() + this.getChannelNo() - 1); // note off
            buffer[numBytes++] = pitch; // pitch
            buffer[numBytes++] = (byte)0; // velocity

            MidiControl.this.sendMidi(buffer,numBytes);
        }

        public void allOff(){
            if(0 == this.getChannelNo()){
                MidiControl.this.toastMassage("Channel is not initialized.");
                return;
            }
            // MIDIデータの作成
            byte[] buffer = new byte[16];
            int numBytes = 0;
            buffer[numBytes++] = (byte)(ChannelVoice.ControlChange.getByte() + this.getChannelNo() - 1);
            buffer[numBytes++] = ChannelMode.AllnoteOff.getByte(); //all note off
            buffer[numBytes++] = (byte)0;

            MidiControl.this.sendMidi(buffer,numBytes);
        }

        public void closeChannel() {
            this.channel = (byte)0;
        }
    }
    private MidiChannel[] midiChannels;

    public enum ChannelVoice {
        NoteOn(0x90),
        NoteOff(0x80),
        keyPressure(0xA0),
        ControlChange(0xB0),
        ProgramChange(0xC0),
        ChannelPressure(0xD0),
        PitchBend(0xE0);

        private int Value;

        private ChannelVoice(int Value){
            this.Value = Value ;
        }
        public byte getByte(){
            return (byte)this.Value ;
        }
    };

    public enum ChannelMode {
        AllSoundOff(0x78),
        ResetAllController(0x79),
        LocalControl(0x7A),
        LocalOn(0x00),
        LocalOff(0x7F),
        AllnoteOff(0x7B),
        OmniOn(0x7D),
        OmniOff(0x7C),
        PolyMode(0x7F),
        MonoMode(0x7E);

        private int Value;

        private ChannelMode(int Value){
            this.Value = Value ;
        }
        public byte getByte(){
            return (byte)this.Value ;
        }
    }

    public enum MIDImode{
        Mode1(1),
        Mode2(2),
        Mode3(3),
        Mode4(4);

        private int Value;

         MIDImode(int Value){
            this.Value = Value ;
        }
    }

    private void toastMassage( String messageText ){
        Toast toast ;

        toast = Toast.makeText(this.context, messageText, Toast.LENGTH_SHORT);
        toast.setGravity(Gravity.CENTER, 0, 0);
        toast.show();
    }

    private synchronized void sendMidi(byte[] buffer, int length){
        if( null == this.inPort){
            this.toastMassage("Midi inport is not assigned.");
            return;
        }
        if( buffer.length < length){
            length = buffer.length;
        }
        try {
            // MIDIデバイスへデータを送信
            this.inPort.send(buffer, 0, length);
        }
        catch (IOException e){
            this.toastMassage("Midi send error.");
        }
    }

    class OnMidiDeviceOpenedListener implements MidiManager.OnDeviceOpenedListener{
        @Override
        public void onDeviceOpened(MidiDevice device) {
            // MIDIデバイスの入力ポートをオープンする。（MIDIデータの入力先）
            MidiControl.this.midiDevice = device ;
            MidiControl.this.inPort = MidiControl.this.midiDevice.openInputPort(MidiControl.this.InputPortNum);
        }
    }
    protected OnMidiDeviceOpenedListener OpenListener ;

    public MidiControl(){
        int i ;

        this.midiChannels = new MidiChannel[16];
        for( i = 0 ; i < this.midiChannels.length ; i++){
            this.midiChannels[i] = new MidiChannel() ;
        }
    }

    public synchronized boolean Initialize(Context context) {
        this.context = context;

        if (null == this.midiManager) {
            this.toastMassage("Midi initialize");
            if (context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_MIDI)) {
                this.midiManager = (MidiManager) context.getSystemService(Context.MIDI_SERVICE);
                this.toastMassage("Midi is supported.");
            }
            else {
                this.toastMassage("Midi is NOT supported.");
            }
        }
        if(null == this.midiManager){
            return false ;
        }
        return true ;
    }

    public MidiDeviceInfo[] getMidiDeviceInfo() {
        return this.midiManager.getDevices();
    }

    public MidiDeviceInfo   getSequencerInfo(){
        if(null != this.midiDevice) {
            return this.midiDevice.getInfo();
        }
        else{
            return  null ;
        }
    }

    public void openMidiDevice(){
        // MIDIデバイスをオープンする。
        MidiDeviceInfo[] deviceInfos = this.getMidiDeviceInfo();
        // 最初に見つけたMIDIシーケンサーに接続する。（暫定仕様。将来的にはMIDIシーケンサーを選択できるようにしたい。）
        int i, j ;
        MIDI_SEARCH_LOOP:   for ( i = 0; i < deviceInfos.length ; i++) {
            MidiDeviceInfo.PortInfo[] portInfos = deviceInfos[i].getPorts();
            for ( j = 0; j < portInfos.length; j++){
                if (MidiDeviceInfo.PortInfo.TYPE_INPUT == portInfos[j].getType()) {
                    this.InputPortNum = j ;
                    this.OpenListener = new OnMidiDeviceOpenedListener() ;
                    // MIDIデバイスをオープンする。
                    this.midiManager.openDevice(deviceInfos[i], this.OpenListener, null);
                    break MIDI_SEARCH_LOOP;
                }
            }
        }
        if( i >= deviceInfos.length ) {
            this.toastMassage("Midi sequencer was NOT found.");
        }
    }

    public synchronized MidiChannel openChannel(int instrument, MIDImode midiMode){
        int i ;
        for( i = 0 ; i < this.midiChannels.length ; i++){
            if(0 == this.midiChannels[i].channel){
                this.midiChannels[i].channel = (byte)(i + 1);
                this.midiChannels[i].setInstrument((byte)instrument, midiMode);
                return this.midiChannels[i];
            }
        }
        return null ;
    }
 }
