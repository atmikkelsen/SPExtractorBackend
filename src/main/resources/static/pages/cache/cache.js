import { makeOptions } from "../../utils/utils.js";
import { loginAndGetToken } from "../../server/auth.js";
import { API_URL } from "../../server/settings.js";

let autoRefreshInterval = null;
let autoRefreshEnabled = true;

// Initialize on page load
document.addEventListener('DOMContentLoaded', () => {
    loadCacheStats();
    startAutoRefresh();

    document.getElementById('refreshBtn').addEventListener('click', loadCacheStats);
});

async function loadCacheStats() {
    try {
        const response = await fetch(`${API_URL}/cache/stats`, makeOptions("GET", null, true));

        if (response.ok) {
            const stats = await response.json();
            updateCacheDisplay(stats);
        } else if (response.status === 401) {
            console.error('Token expired, redirecting to login');
            await loginAndGetToken();
            loadCacheStats(); // Retry after login
        } else {
            console.error('Failed to fetch cache stats:', response.statusText);
        }
    } catch (error) {
        console.error('Error loading cache stats:', error);
        if (error.message.includes('No token found')) {
            await loginAndGetToken();
            loadCacheStats(); // Retry after login
        }
    }
}

function updateCacheDisplay(stats) {
    // Update Sites Cache
    if (stats.sites) {
        document.getElementById('sitesSize').textContent = stats.sites.size;
        document.getElementById('sitesHitRate').textContent = stats.sites.hitRate;
        document.getElementById('sitesHits').textContent = stats.sites.hitCount;
        document.getElementById('sitesMisses').textContent = stats.sites.missCount;
        document.getElementById('sitesEvictions').textContent = stats.sites.evictionCount;
        updateHitRateColor('sitesHitRate', stats.sites.hitRate);
    }

    // Update Drives Cache
    if (stats.drives) {
        document.getElementById('drivesSize').textContent = stats.drives.size;
        document.getElementById('drivesHitRate').textContent = stats.drives.hitRate;
        document.getElementById('drivesHits').textContent = stats.drives.hitCount;
        document.getElementById('drivesMisses').textContent = stats.drives.missCount;
        document.getElementById('drivesEvictions').textContent = stats.drives.evictionCount;
        updateHitRateColor('drivesHitRate', stats.drives.hitRate);
    }

    // Update Files Cache
    if (stats.files) {
        document.getElementById('filesSize').textContent = stats.files.size;
        document.getElementById('filesHitRate').textContent = stats.files.hitRate;
        document.getElementById('filesHits').textContent = stats.files.hitCount;
        document.getElementById('filesMisses').textContent = stats.files.missCount;
        document.getElementById('filesEvictions').textContent = stats.files.evictionCount;
        updateHitRateColor('filesHitRate', stats.files.hitRate);
    }
}

function updateHitRateColor(elementId, hitRateStr) {
    const element = document.getElementById(elementId);
    const hitRate = parseFloat(hitRateStr);
    
    element.classList.remove('excellent', 'good', 'poor');
    
    if (hitRate >= 80) {
        element.classList.add('excellent');
    } else if (hitRate >= 60) {
        element.classList.add('good');
    } else {
        element.classList.add('poor');
    }
}

async function clearCache(cacheName) {
    if (!confirm(`Are you sure you want to clear the ${cacheName} cache?`)) {
        return;
    }

    try {
        const response = await fetch(`${API_URL}/cache/clear/${cacheName}`, makeOptions("POST", null, true));

        if (response.ok) {
            const result = await response.json();
            alert(result.message);
            loadCacheStats(); // Refresh stats
        } else {
            alert('Failed to clear cache');
        }
    } catch (error) {
        console.error('Error clearing cache:', error);
        alert('Error clearing cache');
    }
}

async function clearAllCaches() {
    if (!confirm('Are you sure you want to clear ALL caches? This will remove all cached data.')) {
        return;
    }

    try {
        const response = await fetch(`${API_URL}/cache/clear`, makeOptions("POST", null, true));

        if (response.ok) {
            const result = await response.json();
            alert(result.message);
            loadCacheStats(); // Refresh stats
        } else {
            alert('Failed to clear caches');
        }
    } catch (error) {
        console.error('Error clearing caches:', error);
        alert('Error clearing caches');
    }
}

function startAutoRefresh() {
    if (autoRefreshInterval) {
        clearInterval(autoRefreshInterval);
    }
    autoRefreshInterval = setInterval(loadCacheStats, 5000); // Refresh every 5 seconds
}

function stopAutoRefresh() {
    if (autoRefreshInterval) {
        clearInterval(autoRefreshInterval);
        autoRefreshInterval = null;
    }
}

function toggleAutoRefresh() {
    autoRefreshEnabled = !autoRefreshEnabled;
    const btn = document.getElementById('toggleAutoRefreshBtn');
    const status = document.getElementById('autoRefreshStatus');
    
    if (autoRefreshEnabled) {
        startAutoRefresh();
        btn.textContent = 'Disable Auto-refresh';
        status.textContent = 'Enabled';
    } else {
        stopAutoRefresh();
        btn.textContent = 'Enable Auto-refresh';
        status.textContent = 'Disabled';
    }
}
