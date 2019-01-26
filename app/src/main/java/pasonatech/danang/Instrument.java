package pasonatech.danang;

public class Instrument {
    private MidiControl.MidiChannel channel ;

    public int getMIDIchannel(){
        return this.channel.getChannelNo();
    }

    public int getInstrumentNo(){
        return this.channel.getInstrumentNo();
    }

    public Instrument(MidiControl midiControl, int InstrumentNo, MidiControl.MIDImode midiMode){
        this.channel = midiControl.openChannel(InstrumentNo, midiMode) ;
    }
    // 指定された音を指定された強さで出します。
    public void noteOn( int pitch, int velocity){
        this.channel.noteOn((byte)pitch, (byte)velocity);
    }
    // 指定されたオクターブの指定された音を指定された強さで出します。
    public void noteOn( int octave, int pitch, int velocity){
        byte pitchNo;
        pitchNo = (byte)(octave*12 + pitch%12) ;
        this.channel.noteOn(pitchNo, (byte)velocity);
    }
    // 指定された音を止めます。
    public void noteOff( int pitch){
        this.channel.noteOff((byte)pitch);
    }
    // 指定されたオクターブの指定された音を止めます。
    public void noteOff( int octave, int pitch){
        byte pitchNo;
        pitchNo = (byte)(octave*12 + pitch%12) ;
        this.channel.noteOff(pitchNo);
    }
    // すべての音を止めます。
    public void allOff(){
        this.channel.allOff();
    }
}
