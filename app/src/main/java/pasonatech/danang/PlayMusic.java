package pasonatech.danang;

import java.util.Date;
import java.util.Random;

public class PlayMusic {
    public enum Key{
        C(0),
        Cs(1),
        Df(1),
        D(2),
        Ds(3),
        Ef(3),
        E(4),
        F(5),
        Fs(6),
        Gf(6),
        G(7),
        Gs(8),
        Af(8),
        A(9),
        As(10),
        Bf(10),
        B(11);

        private int Value;

        private Key(int Value){
            this.Value = Value ;
        }

        public int getInt(){
            return (int)this.Value ;
        }
    }

    public enum Scale{
        MajorDiatonic(10),
        MajorPentatonic(11),
        NaturalMinor(20),
        MinorPentatonic(21),
        HarmonicMinor(22),
        MerodicMinor(23),
        MajorBlues(30),
        BlueNote(31),
        Hungarian(100),
        Gipsy(110),
        SpanishPhrygian(120),
        EightNoteSpanish(121);

        private int Value;

        private Scale(int Value){
            this.Value = Value ;
        }
    }

    protected Key key;
    protected Scale scale;
    protected int[] scaleTable ;
    protected Instrument instrument;
    protected int octave;
    protected int topOctave;
    protected int bottumOctave;
    protected int octaveUp;
    protected int octaveDown;

    protected boolean firstPitch ;
    protected int note ;
    protected Random random ;

    /**
     *
     * @param instrument
     * @param key
     * @param upper
     * @param bottum
     */
    public PlayMusic(Instrument instrument, Key key, int upper, int bottum){

        this.key = key ;
        this.Reset();
        this.octaveUp = 15;
        this.octaveDown = 15;
        this.topOctave = this.octave + upper;
        this.bottumOctave = this.octave - bottum;
        this.instrument = instrument;

        Date date = new Date();
        this.random = new Random(date.getTime());
    }

    public void Reset(){
        this.firstPitch = false ;
        this.octave = 60 / 12; // 60 : Center C
    }

    private int decideOvtave() {
        int num = this.random.nextInt(100);

        if (99 - this.octaveUp <= num) {
            if (this.topOctave > this.octave) {
                this.octave++;
            }
        } else if ( this.octaveDown >= num) {
            if (this.bottumOctave < this.octave) {
                this.octave--;
            }
        }
        return this.octave;
    }

    private int decideNote(){
        if( false == this.firstPitch ){
            this.firstPitch = true;
            this.note = this.key.getInt() ;    //  60 is center C.
        }
        this.note = this.key.getInt() + this.scaleTable[this.random.nextInt(scaleTable.length)];
        return this.note ;
    }

    public void Play(){
        this.instrument.allOff();
        this.decideOvtave();
        this.decideNote();
        this.instrument.noteOn(this.octave, this.note,this.random.nextInt(64)+64) ;
    }
    public void Stop(){
        this.instrument.allOff();
    }
    public Key getKey(){
        return this.key;
    }
    public Scale getScale(){
        return this.scale;
    }
    public int getOctave(){ return this.octave; }
    public int getOctaveUp() { return this.octaveUp ;}
    public void setOctaveUp( int up ){ this.octaveUp = up%100 ;}
    public int getOctaveDown() { return this.octaveDown ;}
    public void setOctaveDown( int down ){ this.octaveDown = down%100 ;}
}

class PlayMusicMajor extends PlayMusic{
    public PlayMusicMajor(Instrument instrument, Key key, int upper, int bottum){
        super(instrument, key, upper, bottum);
        this.scale = Scale.MajorDiatonic;
        this.scaleTable = new int[]{0,2,4,5,7,9,11} ;
    }
}

class PlayMusicMajorPentatonic extends PlayMusic{
    public PlayMusicMajorPentatonic(Instrument instrument, Key key, int upper, int bottum){
        super(instrument, key, upper, bottum);
        this.scale = Scale.MajorPentatonic;
        this.scaleTable = new int[]{0,2,4,7,9} ;
    }
}

class PlayMusicNaturalMinor extends PlayMusic{
    public PlayMusicNaturalMinor(Instrument instrument, Key key, int upper, int bottum){
        super(instrument, key, upper, bottum);
        this.scale = Scale.NaturalMinor;
        this.scaleTable = new int[]{0,2,3,5,6,8,10};
    }
}

class PlayMusicHarmonicMinor extends PlayMusic{
    public PlayMusicHarmonicMinor(Instrument instrument, Key key, int upper, int bottum){
        super(instrument, key, upper, bottum);
        this.scale = Scale.HarmonicMinor;
        this.scaleTable = new int[]{0,2,3,5,6,9,10};
    }
}

class PlayMusicMerodicMinor extends PlayMusic{
    private int[][] scaleTable;
    public PlayMusicMerodicMinor(Instrument instrument, Key key, int upper, int bottum){
        super(instrument, key, upper, bottum);
        this.scale = Scale.MerodicMinor;
        this.scaleTable = new int[][]{{0,2,3,5,7,9,11},{0,2,3,5,6,8,10}};
    }

    private int decideOvtave(boolean up) {
        int num = this.random.nextInt(100);

        if ((true == up) && (99 - this.octaveUp <= num)) {
            if (this.topOctave > this.octave) {
                this.octave++;
            }
        } else if ( this.octaveDown >= num) {
            if (this.bottumOctave < this.octave) {
                this.octave--;
            }
        }
        return this.octave;
    }

    private int decideNote(){
        int beforeNote = this.note;

        if( false == this.firstPitch ){
            this.firstPitch = true;
            this.note = this.key.getInt() ;    //  60 is center C.
        }
        else if( 0 != (this.random.nextInt()&1)){
            if( this.key.getInt() + this.scaleTable[0][this.scaleTable[0].length-1] <= this.note){
                if(this.octave < this.topOctave){
                    this.octave++ ;
                    this.note = this.key.getInt() + this.scaleTable[0][this.random.nextInt(scaleTable[0].length)];
                }
            }
            else {
  //              this.decideOvtave( true );
                do {
                    this.note = this.key.getInt() + this.scaleTable[0][this.random.nextInt(scaleTable[0].length)];
                } while (beforeNote > this.note);
            }
        }
        else{
            if( this.key.getInt() >= this.note){
                if(this.octave > this.bottumOctave){
                    this.octave-- ;
                    this.note = this.key.getInt() + this.scaleTable[1][this.random.nextInt(scaleTable[1].length)];
                }
            }
            else {
//                this.decideOvtave( false );
                do {
                    this.note = this.key.getInt() + this.scaleTable[1][this.random.nextInt(scaleTable[1].length)];
                } while (beforeNote < this.note);
            }
        }
        return this.note ;
    }
    public void Play(){
        this.instrument.allOff();
        this.decideNote();
        this.instrument.noteOn(this.octave, this.note,this.random.nextInt(64)+64) ;
    }
}

class PlayMusicMinorPentatonic extends PlayMusic{
    public PlayMusicMinorPentatonic(Instrument instrument, Key key, int upper, int bottum){
        super(instrument, key, upper, bottum);
        this.scale = Scale.MinorPentatonic;
        this.scaleTable = new int[]{0,3,5,7,10};
    }
}

class PlayMusicMajorBlues extends PlayMusic{
    public PlayMusicMajorBlues(Instrument instrument, Key key, int upper, int bottum){
        super(instrument, key, upper, bottum);
        this.scale = Scale.MajorBlues;
        this.scaleTable = new int[]{0,2,3,5,7,9};
    }
}

class PlayMusicBlueNote extends PlayMusic{
    public PlayMusicBlueNote(Instrument instrument, Key key, int upper, int bottum){
        super(instrument, key, upper, bottum);
        this.scale = Scale.BlueNote;
        this.scaleTable = new int[]{0,3,5,6,7,10};
    }
}

class PlayMusicHungarian extends PlayMusic{
    public PlayMusicHungarian(Instrument instrument, Key key, int upper, int bottum){
        super(instrument, key, upper, bottum);
        this.scale = Scale.Hungarian;
        this.scaleTable = new int[]{0,2,3,6,7,8,11};
    }
}

class PlayMusicGipsy extends PlayMusic{
    public PlayMusicGipsy(Instrument instrument, Key key, int upper, int bottum){
        super(instrument, key, upper, bottum);
        this.scale = Scale.Gipsy;
        this.scaleTable = new int[]{0,1,4,5,7,8,11};
    }
}

class PlayMusicSpanishPhrygian extends PlayMusic{
    public PlayMusicSpanishPhrygian(Instrument instrument, Key key, int upper, int bottum){
        super(instrument, key, upper, bottum);
        this.scale = Scale.SpanishPhrygian;
        this.scaleTable = new int[]{0,1,4,5,7,8,10};
    }
}

class PlayMusicEightNoteSpanish extends PlayMusic{
    public PlayMusicEightNoteSpanish(Instrument instrument, Key key, int upper, int bottum){
        super(instrument, key, upper, bottum);
        this.scale = Scale.EightNoteSpanish;
        this.scaleTable = new int[]{0,1,3,4,5,7,8,10};
    }
}
