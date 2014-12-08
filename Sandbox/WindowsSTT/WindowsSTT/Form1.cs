using System;
using System.Collections.Generic;
using System.ComponentModel;
using System.Data;
using System.Drawing;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using System.Windows.Forms;
using System.Speech.Recognition;
using System.Runtime.InteropServices;

namespace WindowsSTT
{
    public partial class Form1 : Form
    {
        public Form1()
        {
            InitializeComponent();
        }

        private void button1_Click(object sender, EventArgs e)
        {
            SpeechRecognitionEngine recognizer = new SpeechRecognitionEngine();
            Grammar dictationGrammar = new DictationGrammar();
            recognizer.LoadGrammar(dictationGrammar);
            try
            {
                button1.Text = "Speak Now";
                recognizer.SetInputToDefaultAudioDevice();
                RecognitionResult result = recognizer.Recognize();
                button1.Text = result.Text;
            }
            catch (InvalidOperationException exception)
            {
                button1.Text = String.Format("Could not recognize input from default aduio device. Is a microphone or sound card available?\r\n{0} - {1}.", exception.Source, exception.Message);
            }
            finally
            {
                recognizer.UnloadAllGrammars();
            }

        }

        private void button3_Click(object sender, EventArgs e)
        {
            SpeechRecognitionEngine recognizer = new SpeechRecognitionEngine();
            Grammar dictationGrammar = new DictationGrammar();
            recognizer.LoadGrammar(dictationGrammar);
            try
            {
                button3.Text = "Recognizing";
                recognizer.SetInputToWaveFile("c:\\InMind\\temp\\fromClient3.wav");

                RecognitionResult result = recognizer.Recognize();
                button3.Text = result.Text;
            }
            catch (InvalidOperationException exception)
            {
                button3.Text = String.Format("Could not recognize input from default aduio device. Is a microphone or sound card available?\r\n{0} - {1}.", exception.Source, exception.Message);
            }
            finally
            {
                recognizer.UnloadAllGrammars();
            }
        }

        private bool isRecording = false;
        [DllImport("winmm.dll", EntryPoint = "mciSendStringA", CharSet = CharSet.Ansi, SetLastError = true, ExactSpelling = true)]
        private static extern int mciSendString(string lpstrCommand, string lpstrReturnString, int uReturnLength, int hwndCallback);
        private void button2_Click(object sender, EventArgs e)
        {
            if (!isRecording)
            {

                mciSendString("open new Type waveaudio Alias recsound", "", 0, 0);
                //string command = "set capture time format ms bitspersample 16 channels 1 samplespersec 16000 alignment 4";
                //var ret = mciSendString("set recsound bitspersample 16", "", 0, 0); doesn't work :(
                //ret = mciSendString("set recsound samplespersec 44100", "", 0, 0);
                //ret = mciSendString("set recsound channels 1", "", 0, 0);

                mciSendString("record recsound", "", 0, 0);
                button2.Text = "Stop Recording...";
                isRecording = true;
            }
            else
            {
                mciSendString("save recsound c:\\InMind\\temp\\result.wav", "", 0, 0);
                mciSendString("close recsound ", "", 0, 0);
                button2.Text = "Record Again";
                isRecording = false;
            }
        }

    }
}
