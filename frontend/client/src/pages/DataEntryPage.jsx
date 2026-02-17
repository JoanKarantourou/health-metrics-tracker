const fetchDropdownData = async () => {
  try {
    setLoading(true);
    setError(null);

    // Fetch both in parallel
    const [facilitiesResponse, indicatorsResponse] = await Promise.all([
      facilityService.getAll({ size: 1000 }),
      indicatorService.getAll(),
    ]);

    // facilityService returns a Page object with .content array
    const facilitiesData =
      facilitiesResponse.data.content || facilitiesResponse.data;
    const indicatorsData = indicatorsResponse.data;

    // Filter only active facilities and indicators
    setFacilities(facilitiesData.filter((f) => f.active));
    setIndicators(indicatorsData.filter((i) => i.active));

    console.log("✅ Loaded dropdown data");
  } catch (err) {
    console.error("❌ Error loading dropdown data:", err);
    setError("Failed to load form data. Please ensure the backend is running.");
  } finally {
    setLoading(false);
  }
};
