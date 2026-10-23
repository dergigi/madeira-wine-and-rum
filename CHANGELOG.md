# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

## [0.6.1] - 2026-10-23

### Changed

- Order wine-colour cards and filters as Rosé, Red, White in both languages.
- Make filter pills smaller while retaining 48dp touch targets; distinguish the secondary sweetness row with smaller outlined pills in both themes.

### Fixed

- Avoid repeating the wine-colour hero photograph in bottle recommendations on the rosé, red and white pages; use a compact bottle link instead.

## [0.6.0] - 2026-10-23

### Added

- System-following dark mode across navigation, detail pages, filters, native controls and the island map.
- Dark launch background, Android system bars and Android 12+ splash artwork.

### Changed

- Use shared light/dark colour resources with readable category labels and selected filters; preserve original photo colours.

## [0.5.0] - 2026-10-23

### Added

- English and German interface, guide articles, places, bottle notes, itinerary and photo captions.
- Persistent flag-based language switch in the header, defaulting to the device language on first launch.
- Full detail screens with header and Android Back navigation; preserve screen history and scroll positions across language changes and activity recreation.

### Changed

- Align bottom-navigation icons and labels in equal-height cells.
- Replace pop-up dialogs and expandable snippets with dedicated pages and visible planning sections.
- Refine typography, related-page links and map controls; add photographs to day, bottle and wine-colour detail screens.
- Replace repeated photo-overlay buttons with credits beneath detail photographs and a complete credits page.


## [0.4.0] - 2026-10-23

### Added

- Red, white and rosé visual wine categories on Discover and Taste.
- Dedicated rosé page with a short explainer, food pairing ideas, Atlantis Rosé and vineyard visit links.
- Atlantis red and white table-wine suggestions supported by producer sources.
- Three credited illustrative wine photographs, available offline.

### Changed

- Keep fortified Madeira Wine and its sweetness filters separate from still-wine colours.
- Preserve wine-category selection across activity recreation and reveal the selected filter automatically.

## [0.3.0] - 2026-10-23

### Added

- Thirteen offline Madeira photographs with photographer credits and Creative Commons license links.
- Curated YouTube links for a Chris Blandy conversation and an Engenhos do Norte rum tour.
- Swipeable story and video cards, photo-led place cards and a visual seven-day itinerary.
- Background image decoding and a bounded bitmap cache.

### Changed

- Replace long browsing-page paragraphs with photo cards, short captions and tap-to-read details.
- Move supporting explanations and visit planning into expandable sections.
- Refresh typography, spacing, colours and native navigation icons.

## [0.2.0] - 2026-10-23

### Added

- Native Discover, Map, One week and Bottles sections, replacing the placeholder welcome screen.
- Madeira Wine and agricultural rum explainers, traditional drink tips and source links.
- Offline island map with pan, zoom, ten numbered places, overlapping-pin selection and an accessible place list.
- Place search, wine/rum/local filters and saved places stored on the device.
- Tasting suggestions, booking information and Google Maps links for each destination.
- Seven-day itinerary grouped by area, plus wine and rum picks by taste.
- Content review dates, source documentation and public-domain map attribution.

## [0.1.1] - 2026-10-22

### Fixed

- Keep “Madeira Wine” together in the app name, welcome screen, and store listing.

### Changed

- CI installs supported Android SDK packages without the retired `tools` package.

## [0.1.0] - 2026-10-22

### Added

- Native Android welcome screen with original Madeira, wine, and rum artwork.
- Adaptive launcher icon and Android system splash screen.
- Signed release builds and Zapstore publishing using the existing Feather signing setup.
- Gradle wrapper, build workflow, and release documentation.

[Unreleased]: https://github.com/dergigi/madeira-wine-and-rum/compare/v0.6.1...HEAD
[0.6.1]: https://github.com/dergigi/madeira-wine-and-rum/compare/v0.6.0...v0.6.1
[0.6.0]: https://github.com/dergigi/madeira-wine-and-rum/compare/v0.5.0...v0.6.0
[0.5.0]: https://github.com/dergigi/madeira-wine-and-rum/compare/v0.4.0...v0.5.0
[0.4.0]: https://github.com/dergigi/madeira-wine-and-rum/compare/v0.3.0...v0.4.0
[0.3.0]: https://github.com/dergigi/madeira-wine-and-rum/compare/v0.2.0...v0.3.0
[0.2.0]: https://github.com/dergigi/madeira-wine-and-rum/compare/v0.1.1...v0.2.0
[0.1.1]: https://github.com/dergigi/madeira-wine-and-rum/compare/v0.1.0...v0.1.1
[0.1.0]: https://github.com/dergigi/madeira-wine-and-rum/releases/tag/v0.1.0
