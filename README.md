# Loot Radar 🎮📡

📲 **Get it on Google Play:** [Loot Radar on the Play Store](https://play.google.com/store/apps/details?id=com.radarlabs.freegameradar)

Loot Radar is a free game giveaway tracker built around one core idea: **notifications should be precise, not noisy.**

Instead of dumping every possible deal on you, Loot Radar lets you pick exactly which platforms and giveaway types you care about — then only notifies you about those.

## The Problem

Free game giveaways are scattered across Reddit threads, Discord servers, and half a dozen tracker apps — most of which are built around lists, not around getting the *notification* experience right. Reddit in particular buries giveaway posts under discussions and comments that have nothing to do with "what's free right now."

Loot Radar exists to fix that: **tell me when a game goes free, and only tell me about the things I actually asked for.**

## Features

- **Filtered push notifications** — choose your platforms and giveaway types; everything else is filtered out
- **Multi-platform tracking** — Epic Games Store, Steam, GOG, Ubisoft Connect, Battle.net, EA, itch.io, Xbox, PlayStation, Nintendo, mobile, VR, and DRM-free titles
- **Home feed** — active giveaways with cover art, platform, and expiration time
- **Hot Deals** — surfaces giveaways getting the most attention
- **Stats** — tracks games collected and estimated value saved
- **Optional accounts** — usable anonymously, or sign in to sync preferences across devices

## How It Works

The interesting engineering problem here isn't the notification itself, it's the pipeline behind it. Three things have to hold true at scale:

1. **Detect state changes reliably.** Scheduled background jobs poll giveaway sources and diff the results against last-known state, so the system knows exactly what's *new* rather than re-processing the same offers every run.
2. **Match diffs against thousands of filter combinations.** Each user can combine platforms and giveaway types in their own way. Every detected change gets run through each user's filter set to determine who actually cares.
3. **Fire exactly once.** No duplicate notifications for the same offer, and no missed matches. The diff-then-filter order (diff first, filter second) is what keeps this from turning into a spam pipeline; it's closer to an event-processing/diffing problem than a typical mobile CRUD flow.

This is also why sign-in syncing works instantly across devices: filter preference changes propagate through Firestore's real-time sync, so the matching step always reads current preferences rather than stale ones.

## Tech Stack

- **Kotlin Multiplatform (KMP)** — shared application logic: data models, filtering rules, and the matching engine live in one place instead of being duplicated per platform
- **Compose Multiplatform / Jetpack Compose** — UI, built with **Material Design 3**
- **Firebase Authentication** — user accounts
- **Cloud Firestore** — data storage and **real-time sync** for preferences and giveaway data
- **Background/scheduled tasks** — poll sources, diff against last known state, and filter per-user before any notification fires
- **Push notifications** — filtered delivery based on user preferences
- **GitHub Actions** — CI/CD

## Getting Started

### Prerequisites

- [Android Studio](https://developer.android.com/studio) (latest stable version recommended)
- JDK 17+
- A Firebase project (for Authentication and Firestore) if you want auth/sync to work locally

### Setup

1. Clone the repo:
```bash
   git clone https://github.com/KartikPat250905/loot-radar.git
```
2. Open the project in **Android Studio** (`File > Open`, select the project root).
3. Let Gradle sync — Android Studio will download the required KMP/Compose dependencies automatically.
4. Add your own `google-services.json` file to the app module if you want Firebase Authentication and Firestore to work with your own backend. *(Not included in this repo for security reasons.)*
5. Select the `androidApp` run configuration and hit **Run** ▶️ on an emulator or physical device.

That's it — no separate build steps or CLI setup needed beyond what Android Studio handles for you.

## Project Status

Loot Radar is live on the **Google Play Store** and under active development. Ongoing work is focused on improving giveaway tracking accuracy, notification timing, and overall app experience.

## Feedback

If you try the app, I'd genuinely appreciate feedback — good or bad:

- Leave a review on the **Google Play Store**
- Or email **radarlabs.dev@gmail.com**

## Contributing

Issues and pull requests are welcome. If you're planning a larger change, please open an issue first to discuss what you'd like to do.

## License

Apache 2.0
