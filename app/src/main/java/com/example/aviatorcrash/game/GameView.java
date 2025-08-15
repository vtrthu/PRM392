package com.example.aviatorcrash.game;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RadialGradient;
import android.graphics.RectF;
import android.graphics.Shader;
import android.util.AttributeSet;
import android.view.View;

import com.example.aviatorcrash.R;

/**
 * Custom view that renders a beautiful airplane using images and animates its position
 * across the screen based on the current game progress/multiplier.
 */
public class GameView extends View {

	private final Paint planePaint;
	private final Paint trailPaint;
	private final Paint gridPaint;
	private final Paint glowPaint;
	private final Paint shadowPaint;
	
	// Reusable Paint objects for crash effects (prevent memory leaks)
	private final Paint explosionPaint;
	private final Paint particlePaint;
	private final Paint shockWavePaint;
	private final Paint firePaint;
	private final Paint sparkPaint;
	private final Paint flashPaint;

	private Bitmap airplaneBitmap;
	private Bitmap airplaneBitmapScaled;
	private Matrix transformMatrix;

	private float progressNormalized; // 0..1 mapped from multiplier/time
	private boolean isFlying;
	private boolean isCrashed;
	private long crashStartTime = 0; // Time when crash effect started
	private boolean crashEffectActive = false;
	private float screenShakeX = 0f;
	private float screenShakeY = 0f;

	private int viewWidth;
	private int viewHeight;

	public GameView(Context context) {
		super(context);
		planePaint = createPlanePaint();
		trailPaint = createTrailPaint();
		gridPaint = createGridPaint();
		glowPaint = createGlowPaint();
		shadowPaint = createShadowPaint();
		
		// Initialize crash effect paints once
		explosionPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		particlePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		shockWavePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		firePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		sparkPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		flashPaint = new Paint();
		
		initAirplane();
	}

	public GameView(Context context, AttributeSet attrs) {
		super(context, attrs);
		planePaint = createPlanePaint();
		trailPaint = createTrailPaint();
		gridPaint = createGridPaint();
		glowPaint = createGlowPaint();
		shadowPaint = createShadowPaint();
		
		// Initialize crash effect paints once
		explosionPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		particlePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		shockWavePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		firePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		sparkPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		flashPaint = new Paint();
		
		initAirplane();
	}

	public GameView(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		planePaint = createPlanePaint();
		trailPaint = createTrailPaint();
		gridPaint = createGridPaint();
		glowPaint = createGlowPaint();
		shadowPaint = createShadowPaint();
		
		// Initialize crash effect paints once
		explosionPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		particlePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		shockWavePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		firePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		sparkPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		flashPaint = new Paint();
		
		initAirplane();
	}

	private void initAirplane() {
		try {
			// Load vector drawable and convert to bitmap
			android.graphics.drawable.Drawable vectorDrawable = androidx.core.content.ContextCompat.getDrawable(getContext(), R.drawable.ic_logofpt);
			if (vectorDrawable != null) {
				// Create bitmap from vector drawable
				int width = 200; // Base size for vector
				int height = 200;
				airplaneBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
				Canvas canvas = new Canvas(airplaneBitmap);
				vectorDrawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
				vectorDrawable.draw(canvas);
				
				transformMatrix = new Matrix();
				
				// Debug logging
				android.util.Log.d("GameView", "FPT Logo vector loaded and converted to bitmap successfully: " + 
					airplaneBitmap.getWidth() + "x" + airplaneBitmap.getHeight());
			} else {
				android.util.Log.e("GameView", "Failed to load FPT Logo vector drawable");
				airplaneBitmap = null;
			}
		} catch (Exception e) {
			android.util.Log.e("GameView", "Error loading FPT Logo: " + e.getMessage());
			e.printStackTrace();
			// Fallback to default airplane if image loading fails
			airplaneBitmap = null;
		}
	}

	private Paint createPlanePaint() {
		Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
		p.setFilterBitmap(true);
		return p;
	}

	private Paint createTrailPaint() {
		Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
		p.setColor(0x66FFFFFF);
		p.setStyle(Paint.Style.STROKE);
		p.setStrokeWidth(8f);
		return p;
	}

	private Paint createGridPaint() {
		Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
		p.setColor(0x22FFFFFF);
		p.setStyle(Paint.Style.STROKE);
		p.setStrokeWidth(1f);
		return p;
	}

	private Paint createGlowPaint() {
		Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
		p.setColor(0x66FFFFFF);
		p.setStyle(Paint.Style.FILL);
		return p;
	}

	private Paint createShadowPaint() {
		Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
		p.setColor(0x44000000);
		p.setStyle(Paint.Style.FILL);
		return p;
	}

	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		super.onSizeChanged(w, h, oldw, oldh);
		viewWidth = w;
		viewHeight = h;
		scaleAirplaneBitmap();
	}

	private void scaleAirplaneBitmap() {
		if (airplaneBitmap != null) {
			// Scale bitmap to appropriate size for the view
			// FPT logo converted from vector (200x200), scale appropriately
			float scale = Math.min(viewWidth, viewHeight) / 300f; // Scale factor for converted FPT logo
			airplaneBitmapScaled = Bitmap.createScaledBitmap(
				airplaneBitmap, 
				(int)(airplaneBitmap.getWidth() * scale), 
				(int)(airplaneBitmap.getHeight() * scale), 
				true
			);
			
			// Debug logging
			android.util.Log.d("GameView", "FPT Logo bitmap scaled: " + 
				airplaneBitmapScaled.getWidth() + "x" + airplaneBitmapScaled.getHeight() + 
				" (scale: " + scale + ")");
		} else {
			android.util.Log.e("GameView", "Cannot scale: airplaneBitmap is null");
		}
	}

	@Override
	protected void onDraw(Canvas canvas) {
		try {
			super.onDraw(canvas);

			// Safety check
			if (viewWidth <= 0 || viewHeight <= 0) {
				return;
			}

			// Apply screen shake if crash effect is active
			boolean shakeApplied = false;
			if (isCrashed && crashEffectActive && Math.abs(screenShakeX) < 100 && Math.abs(screenShakeY) < 100) {
				canvas.save();
				canvas.translate(screenShakeX, screenShakeY);
				shakeApplied = true;
			}

			// background grid for sense of motion
			drawGrid(canvas);

			// draw flight trail
			drawTrail(canvas);

			// draw airplane
			drawPlane(canvas);

			// Restore canvas if shake was applied
			if (shakeApplied) {
				canvas.restore();
			}
		} catch (Exception e) {
			android.util.Log.e("GameView", "Error in onDraw: " + e.getMessage());
			// Reset crash effect if drawing fails
			crashEffectActive = false;
			screenShakeX = 0f;
			screenShakeY = 0f;
		}
	}

	private void drawGrid(Canvas canvas) {
		int step = Math.max(40, Math.min(viewWidth, viewHeight) / 12);
		for (int x = 0; x < viewWidth; x += step) {
			canvas.drawLine(x, 0, x, viewHeight, gridPaint);
		}
		for (int y = 0; y < viewHeight; y += step) {
			canvas.drawLine(0, y, viewWidth, y, gridPaint);
		}
	}

	private void drawTrail(Canvas canvas) {
		float t = clamp(progressNormalized, 0f, 1f);
		
		// Trail follows authentic Aviator diagonal upward curve
		if (viewWidth <= 0 || viewHeight <= 0) {
			return; // Skip if view not ready
		}
		
		// Starting point matches plane starting position
		float startX = dp(30);
		float startY = viewHeight - dp(50);
		
		// Draw smooth curved trail following the same path as plane
		Path trailPath = new Path();
		trailPath.moveTo(startX, startY);
		
		int segments = 25; // Smooth segments for curved trail
		for (int i = 1; i <= segments; i++) {
			float segmentT = (float) i / segments * t;
			
			// Calculate trail position (same logic as plane)
			float endX = viewWidth - dp(30);
			float endY = dp(50);
			
			// Smooth curve trajectory matching plane
			float curveT = easeOutQuad(segmentT);
			float segmentX = lerp(startX, endX, curveT);
			
			float verticalProgress = easeOutCubic(segmentT);
			float segmentY = lerp(startY, endY, verticalProgress);
			
			// Add subtle arc for realistic trail path
			float arcHeight = dp(40);
			float arcProgress = (float) Math.sin(segmentT * Math.PI);
			segmentY -= arcProgress * arcHeight;
			
			trailPath.lineTo(segmentX, segmentY);
		}
		
		// Draw multiple trail lines for better effect
		for (int i = 0; i < 3; i++) {
			float alpha = 0.35f - (i * 0.08f);
			trailPaint.setAlpha((int)(alpha * 255));
			trailPaint.setStrokeWidth(6f - (i * 1.5f));
			trailPaint.setStyle(Paint.Style.STROKE);
			canvas.drawPath(trailPath, trailPaint);
		}
	}

	private void drawPlane(Canvas canvas) {
		float t = clamp(progressNormalized, 0f, 1f);
		
		// Debug logging (disabled for performance)
		// android.util.Log.d("GameView", "drawPlane: t=" + t + ", viewWidth=" + viewWidth + ", viewHeight=" + viewHeight);
		
		// AUTHENTIC AVIATOR MOVEMENT: Bottom-left to top-right with smooth curve
		float cx, cy;
		
		if (viewWidth > 0 && viewHeight > 0) {
			// Starting point: bottom-left corner (like real Aviator game)
			float startX = dp(30);
			float startY = viewHeight - dp(50);
			
			// Ending point: top-right area
			float endX = viewWidth - dp(30);
			float endY = dp(50);
			
			// Smooth curve trajectory (not straight line)
			// Use easeOutQuad for realistic acceleration then deceleration
			float curveT = easeOutQuad(t);
			
			// Horizontal movement with curve
			cx = lerp(startX, endX, curveT);
			
			// Vertical movement with smooth upward curve
			// Add slight curve to make it more natural (not perfectly straight)
			float verticalProgress = easeOutCubic(t);
			cy = lerp(startY, endY, verticalProgress);
			
			// Add subtle arc for more realistic flight path
			float arcHeight = dp(40);
			float arcProgress = (float) Math.sin(t * Math.PI); // Peak at middle
			cy -= arcProgress * arcHeight;
			
		} else {
			// Fallback for uninitialized view
			float curveT = easeOutQuad(t);
			cx = lerp(30f, 750f, curveT);
			cy = lerp(1000f, 50f, easeOutCubic(t));
			cy -= (float) Math.sin(t * Math.PI) * 40f;
		}
		
		// Ensure logo stays within screen bounds with margins
		float logoSize = dp(80);
		float margin = logoSize / 2f + dp(20); // Extra margin for safety
		if (viewWidth > 0 && viewHeight > 0) {
			cx = clamp(cx, margin, viewWidth - margin);
			cy = clamp(cy, margin, viewHeight - margin);
		}

		// Size increases slightly as it "flies away" (perspective effect)
		float baseSize = dp(70);
		float sizeMultiplier = 1f + (t * 0.4f); // Gradual size increase like real Aviator
		float size = baseSize * sizeMultiplier;
		
		// Draw shadow first
		drawPlaneShadow(canvas, cx, cy, size);
		
		// Draw glow effect
		drawPlaneGlow(canvas, cx, cy, size);
		
		// Draw airplane using bitmap or fallback to vector
		if (airplaneBitmapScaled != null) {
			drawPlaneBitmap(canvas, cx, cy, size);
		} else {
			drawPlaneFallback(canvas, cx, cy, size);
		}
		
		if (isCrashed) {
			drawCrashEffect(canvas, cx, cy, size);
		}
	}
	
	private float calculatePlaneX(float t) {
		// Enhanced X movement: starts left, curves more to the right
		float startX = dp(40); // Start a bit more to the right
		float midX = viewWidth * 0.6f; // Middle point further right
		float endX = viewWidth - dp(60); // End with more margin
		
		if (t < 0.5f) {
			// First half: accelerate rightward
			float t1 = t * 2f; // 0 to 1
			return lerp(startX, midX, easeOutQuad(t1));
		} else {
			// Second half: continue right but slow down
			float t2 = (t - 0.5f) * 2f; // 0 to 1
			return lerp(midX, endX, easeInQuad(t2));
		}
	}
	
	private float calculatePlaneY(float t) {
		// Enhanced Y movement: smoother upward curve
		float startY = viewHeight - dp(80); // Start higher from bottom
		float endY = dp(80); // End lower from top
		
		// Use compound easing for very smooth movement
		float smoothT = smoothStep(smoothStep(t)); // Double smooth for ultra-smooth
		return lerp(startY, endY, smoothT);
	}

	private void drawPlaneShadow(Canvas canvas, float cx, float cy, float size) {
		// Draw shadow below the airplane
		RectF shadowRect = new RectF(
			cx - size * 0.8f, 
			cy + size * 0.3f, 
			cx + size * 0.8f, 
			cy + size * 0.5f
		);
		canvas.drawOval(shadowRect, shadowPaint);
	}

	private void drawPlaneGlow(Canvas canvas, float cx, float cy, float size) {
		// Create radial gradient for glow effect
		RadialGradient glowGradient = new RadialGradient(
			cx, cy, size * 2,
			new int[]{0x66FFFFFF, 0x22FFFFFF, 0x00FFFFFF},
			new float[]{0.0f, 0.7f, 1.0f},
			Shader.TileMode.CLAMP
		);
		glowPaint.setShader(glowGradient);
		canvas.drawCircle(cx, cy, size * 2, glowPaint);
		glowPaint.setShader(null);
	}

	private void drawPlaneBitmap(Canvas canvas, float cx, float cy, float size) {
		if (airplaneBitmapScaled == null) {
			android.util.Log.e("GameView", "drawPlaneBitmap: airplaneBitmapScaled is null");
			return;
		}
		
		// Drawing FPT logo at position (cx, cy) with size
		
		// Calculate rotation angle based on flight direction
		float angle = (float) Math.toDegrees(Math.atan2(
			cy - (viewHeight - dp(24)), 
			cx - dp(16)
		));
		
		// Save canvas state
		canvas.save();
		
		// Move to center position
		canvas.translate(cx, cy);
		
		// Rotate around center
		canvas.rotate(angle);
		
		// Calculate final size
		float displaySize = size * 0.8f; // Make it slightly smaller for better visual
		float scaleX = displaySize / airplaneBitmapScaled.getWidth();
		float scaleY = displaySize / airplaneBitmapScaled.getHeight();
		
		// Ensure minimum size for visibility
		float minScale = 0.1f;
		scaleX = Math.max(scaleX, minScale);
		scaleY = Math.max(scaleY, minScale);
		
		// Scale around center
		canvas.scale(scaleX, scaleY);
		
		// Draw bitmap centered at origin
		canvas.drawBitmap(airplaneBitmapScaled, 
			-airplaneBitmapScaled.getWidth() / 2f,
			-airplaneBitmapScaled.getHeight() / 2f, 
			planePaint);
		
		// Restore canvas state
		canvas.restore();
	}

	private void drawPlaneFallback(Canvas canvas, float cx, float cy, float size) {
		// Fallback to a simple but better-looking airplane if bitmap fails
		Paint bodyPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		bodyPaint.setColor(0xFF2196F3);
		bodyPaint.setStyle(Paint.Style.FILL);
		
		Paint wingPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		wingPaint.setColor(0xFF1976D2);
		wingPaint.setStyle(Paint.Style.FILL);
		
		// Draw airplane body (ellipse)
		RectF bodyRect = new RectF(
			cx - size * 0.6f, 
			cy - size * 0.2f, 
			cx + size * 0.6f, 
			cy + size * 0.2f
		);
		canvas.drawOval(bodyRect, bodyPaint);
		
		// Draw wings
		RectF wingRect = new RectF(
			cx - size * 0.8f, 
			cy - size * 0.4f, 
			cx - size * 0.2f, 
			cy + size * 0.4f
		);
		canvas.drawOval(wingRect, wingPaint);
		
		// Draw tail
		Path tail = new Path();
		tail.moveTo(cx - size * 0.6f, cy - size * 0.2f);
		tail.lineTo(cx - size * 0.8f, cy - size * 0.4f);
		tail.lineTo(cx - size * 0.7f, cy - size * 0.3f);
		tail.close();
		canvas.drawPath(tail, wingPaint);
	}

	private void drawCrashEffect(Canvas canvas, float cx, float cy, float size) {
		try {
			if (!crashEffectActive) {
				crashStartTime = System.currentTimeMillis();
				crashEffectActive = true;
			}
			
			long elapsedTime = System.currentTimeMillis() - crashStartTime;
			float animationProgress = Math.min(elapsedTime / 1500f, 1f); // Reduced to 1.5 second
			
			// Safer screen shake (reduced intensity)
			float shakeIntensity = dp(10) * (1f - animationProgress);
			screenShakeX = (float) (Math.random() - 0.5) * shakeIntensity;
			screenShakeY = (float) (Math.random() - 0.5) * shakeIntensity;
			
			// Simplified explosion effects
			drawSimpleExplosion(canvas, cx, cy, size, animationProgress);
			drawSimpleParticles(canvas, cx, cy, size, animationProgress);
			
			// Screen flash effect (safer)
			if (animationProgress < 0.1f && viewWidth > 0 && viewHeight > 0) {
				flashPaint.setColor(0xFFFFFFFF);
				flashPaint.setAlpha((int)(100 * (0.1f - animationProgress) * 10f)); // Reduced alpha
				canvas.drawRect(0, 0, viewWidth, viewHeight, flashPaint);
			}
			
			// Continue animation
			if (animationProgress < 1f) {
				invalidate();
			} else {
				// Reset shake when animation is done
				screenShakeX = 0f;
				screenShakeY = 0f;
			}
		} catch (Exception e) {
			// Fallback: disable crash effect if any error occurs
			android.util.Log.e("GameView", "Error in crash effect: " + e.getMessage());
			crashEffectActive = false;
			screenShakeX = 0f;
			screenShakeY = 0f;
		}
	}
	
	private void drawSimpleExplosion(Canvas canvas, float cx, float cy, float size, float progress) {
		try {
			// Simple explosion circles (no gradients to avoid memory issues)
			for (int i = 1; i <= 3; i++) {
				float radius = size * i * progress;
				int alpha = (int)(150 * (1f - progress) / i);
				
				explosionPaint.setColor(0xFFFF4444);
				explosionPaint.setAlpha(alpha);
				explosionPaint.setStyle(Paint.Style.STROKE);
				explosionPaint.setStrokeWidth(size * 0.1f);
				
				canvas.drawCircle(cx, cy, radius, explosionPaint);
			}
			
			// Solid center explosion
			explosionPaint.setStyle(Paint.Style.FILL);
			explosionPaint.setColor(0xFFFF8800);
			explosionPaint.setAlpha((int)(200 * (1f - progress)));
			canvas.drawCircle(cx, cy, size * 0.8f * progress, explosionPaint);
			
		} catch (Exception e) {
			android.util.Log.e("GameView", "Error in simple explosion: " + e.getMessage());
		}
	}
	
	private void drawSimpleParticles(Canvas canvas, float cx, float cy, float size, float progress) {
		try {
			// Simple particles (reduced count to prevent crashes)
			for (int i = 0; i < 8; i++) {
				float angle = (float) (i * Math.PI * 2 / 8);
				float distance = size * 2f * progress;
				float particleX = cx + (float) Math.cos(angle) * distance;
				float particleY = cy + (float) Math.sin(angle) * distance;
				
				// Simple solid color particles
				int[] colors = {0xFFFF4444, 0xFFFF8800, 0xFFFFAA00, 0xFFFF6600};
				particlePaint.setColor(colors[i % colors.length]);
				particlePaint.setAlpha((int)(180 * (1f - progress)));
				particlePaint.setStyle(Paint.Style.FILL);
				
				float particleSize = size * 0.15f * (1f - progress * 0.5f);
				canvas.drawCircle(particleX, particleY, particleSize, particlePaint);
			}
		} catch (Exception e) {
			android.util.Log.e("GameView", "Error in simple particles: " + e.getMessage());
		}
	}

	private float dp(float value) {
		return value * getResources().getDisplayMetrics().density;
	}

	private float lerp(float a, float b, float t) {
		return a + (b - a) * t;
	}

	private float easeOutCubic(float t) {
		float p = 1f - t;
		return 1f - p * p * p;
	}
	
	// Additional easing functions for smoother animation
	private float easeOutQuad(float t) {
		return 1f - (1f - t) * (1f - t);
	}
	
	private float easeInQuad(float t) {
		return t * t;
	}
	
	private float smoothStep(float t) {
		// Smooth step function for ultra-smooth transitions
		return t * t * (3f - 2f * t);
	}
	
	private float easeInOutCubic(float t) {
		return t < 0.5f ? 4f * t * t * t : 1f - (float)Math.pow(-2f * t + 2f, 3f) / 2f;
	}

	private float clamp(float v, float min, float max) {
		return Math.max(min, Math.min(max, v));
	}

	public void startFlight() {
		isFlying = true;
		isCrashed = false;
		invalidate();
	}

	public void crash() {
		isFlying = false;
		isCrashed = true;
		crashEffectActive = false; // Reset crash effect for new animation
		invalidate();
	}

	public void reset() {
		isFlying = false;
		isCrashed = false;
		crashEffectActive = false;
		crashStartTime = 0;
		screenShakeX = 0f;
		screenShakeY = 0f;
		progressNormalized = 0f;
		invalidate();
	}

	/**
	 * Update airplane position based on current multiplier.
	 * Multiplier typically grows from 1.0 upwards; we normalize it to 0..1.
	 */
	public void setMultiplier(Double multiplier) {
		if (multiplier == null) return;
		// normalize: 1x..11x maps to 0..1; clamp
		float t = (float) ((multiplier - 1.0) / 10.0);
		progressNormalized = clamp(t, 0f, 1f);
		
		// Debug logging (disabled for performance)
		// android.util.Log.d("GameView", "setMultiplier: multiplier=" + multiplier + 
		//	", t=" + t + ", progressNormalized=" + progressNormalized + ", isFlying=" + isFlying);
		
		// Always invalidate to force redraw - FIX for logo not moving
		invalidate();
	}
}


