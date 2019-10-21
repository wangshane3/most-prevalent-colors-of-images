package com.pex.jpg;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import javax.imageio.ImageIO;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

@SpringBootApplication
public class JpgApplication {
	private static final String infileName = "input.txt";
	private static final String outfileName = "result1.txt";
	private static int MAX_COLOR = 0xFFFFFF;

	public static void main(String[] args) {
		SpringApplication.run(JpgApplication.class, args);
		downloadImage();
	}

	private static void downloadImage() {
		final Set<String> visited = new HashSet<>();
		String url = "";
		try (BufferedReader in = new BufferedReader(new FileReader(infileName));
		    BufferedWriter out = new BufferedWriter(new FileWriter(outfileName, true))) {
			while ((url = in.readLine()) != null) {
				if (visited.contains(url)) continue; // skip crawled sites
				visited.add(url); // mark the site as visited
				try { // download from url and process the image
					byte[] image = WebClient.create(url).get().accept(MediaType.IMAGE_JPEG)
							.retrieve().bodyToMono(byte[].class).block();
					processImage(image, out, url);
				} catch (IOException|WebClientResponseException ignored) {
					System.out.println(ignored.getMessage()); // TODO use log
				} // skip it and do nothing if the site is down or file can't IO
			}
			System.out.println("All files processed");
		} catch (IOException ignored) {
			System.out.println(ignored.getMessage());
		} // skip it and do nothing if the file can't IO
	}

	private static void processImage(byte[] image, BufferedWriter out, String url) throws IOException {
		final BufferedImage img = ImageIO.read(new ByteArrayInputStream(image));
		out.write(String.format("%s,%s\n", url, findKmax(countColor(img), 3)));
	}

	// count the occurrence of each color
	private static int[] countPixel(BufferedImage img) {
		int[] counts = new int[MAX_COLOR + 1]; // traverse each pixel and count its occurrence
		for (int i = 0; i < img.getWidth(); i++)
			for (int j = 0; j < img.getHeight(); j++)
				counts[img.getRGB(i, j) & MAX_COLOR]++;
		return counts;
	}

	// return index of k largest elements, return as csv string of hex
	public static String findKmax(int arr[], int k) {
		int[] max = new int[k]; // largest k elements in natural order
		int[] index = new int[k]; // corresponding index ( which is color in this case)
		for (int i = 0; i < arr.length; i++) {
			if (arr[i] <= max[0]) continue;
			int j;
			for (j = 0; j + 1 < k && max[j + 1] < arr[i]; j++) {
				max[j] = max[j + 1];
				index[j] = index[j + 1];
			}
			max[j] = arr[i];
			index[j] = i;
		}
		return IntStream.of(index).mapToObj(n -> String.format("0x%06X", n)).collect(Collectors.joining(","));
	}

	// count the occurrence of each color
	private static Map<Color, Integer> countColor(BufferedImage img) {
		// traverse each pixel and count its occurrence
		Map<Color, Integer> mapColorCount = new HashMap<>();
		for (int i = 0; i < img.getWidth(); i++)
			for (int j = 0; j < img.getHeight(); j++) {
				final Color color = new Color(img.getRGB(i, j)); // or use mapColorCount
				mapColorCount.put(color, 1 + mapColorCount.getOrDefault(color, 0));
			}
		return mapColorCount;
	}

	public static String findKmax(Map<Color, Integer> map, int k) {
		int[] max = new int[k];
		Color[] colors = new Color[k];
		map.forEach((c, n) -> {
			if (n > max[0]) {
				int j;
				for (j = 0; j + 1 < k && max[j + 1] < n; j++) {
					max[j] = max[j + 1];
					colors[j] = colors[j + 1];
				}
				max[j] = n;
				colors[j] = c;				
			}});
		return Arrays.stream(colors).map(c -> String.format("0x%06X", c.getRGB() & MAX_COLOR))
				.collect(Collectors.joining(","));
	}
}