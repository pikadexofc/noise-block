# NoiseBlock 🎧⚡

**NoiseBlock** is an ultra-lightweight, battery-optimized outside noise canceller that works across Android phones, iPhones, laptops, and desktop browsers.

It includes mathematical acoustic synthesizers for **Brown Noise, Pink Noise, White Noise, Green Noise, Box Fan, Steady Rain, Ocean Waves, and Airplane Cabin**, with an acoustic optimization profile specially designed for small phone and laptop speakers.

---

## 🚀 Live Web App & APK Download

- **Live Web App (GitHub Pages):** Open `docs/index.html` in any browser, or enable GitHub Pages in your repository settings:
  - Go to **Settings** > **Pages**
  - Under **Build and deployment**, select **Deploy from a branch**
  - Choose branch: `main` (or `master`) and folder: `/docs`
  - Click **Save** — Your web app will be live at `https://<your-username>.github.io/<repo-name>/`!
- **Download Android APK:** Download directly from [`docs/NoiseBlock.apk`](docs/NoiseBlock.apk) or from GitHub Actions Artifacts / Releases.

---

## 💡 Key Features

### 1. Ultra-Low Power Consumption
- **Zero CPU Looping Overhead:** The audio waveform is generated once into a seamless looping PCM buffer and offloaded to hardware audio DSP / AudioTrack (`MODE_STATIC` on Android, Web Audio Buffer on web). 
- **100% Offline:** Zero Wi-Fi, streaming, or cellular data required.
- **True Black OLED Mode:** AMOLED-friendly interface minimizes display battery drain.

### 2. Mobile & Laptop Speaker Clarity Boost
Small phone and laptop speakers cannot reproduce frequencies below ~160 Hz. Driving deep sub-bass into tiny speakers wastes battery power and causes audio distortion.
- **Speaker Boost:** Rolls off inaudible `<180 Hz` sub-bass to protect phone battery and diaphragms, while boosting the `1.6 kHz` speech and noise masking band for crystal-clear cancellation through built-in phone speakers.
- **Full Range:** Preserves uncompressed sub-bass down to 20 Hz for headphones and hi-fi speakers.

### 3. Eight Psychoacoustic Noise Profiles
1. **Brown Noise (1/f²):** Deep waterfall cascade. Blocks highway traffic, rumbling HVAC, construction, and vibrating walls.
2. **Pink Noise (1/f):** Balanced natural falloff. Masks open-office chatter and keyboard typing.
3. **White Noise:** Equal frequency spectrum. Cancels sudden sharp spikes, screeching sounds, and barking dogs.
4. **Green Noise:** Nature mid-band centered at 500 Hz. Calming background for reading and tinnitus relief.
5. **Box Fan:** Low airflow hum with subtle 12 Hz blade flutter.
6. **Steady Rain:** Soothing drizzle patter with Poisson droplet impacts.
7. **Ocean Surf:** Rhythmic sinusoidal wave swell envelope.
8. **Airplane Cabin:** Pressurized cruising jet engine hum with dual turbine resonance.

### 4. Sleep & Focus Timer
- Presets for 15m, 30m, 45m, 1h, and 1.5h.
- Automatically initiates a smooth 15-second gentle volume fade-out before stopping to avoid startling the user.

---

## 🛠️ How to Publish to GitHub from Google AI Studio

1. In the upper-right corner or settings menu of **Google AI Studio**, click **"Export to GitHub"** (or **"Push to GitHub"**).
2. Connect your GitHub account and select a new or existing repository.
3. Once pushed, enable **GitHub Pages**:
   - Navigate to your repository on GitHub.
   - Click **Settings** > **Pages**.
   - Source: **Deploy from a branch** -> `main` / `/docs` -> **Save**.
4. The GitHub Actions workflow in `.github/workflows/deploy.yml` will automatically build the APK and deploy your web app!
